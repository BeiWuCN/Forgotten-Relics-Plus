package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 日耀石（shiny_stone）的「日耀能量」，1.7.10 原版 {@code EntityShinyEnergy}
 * （RE 的 {@code entities/EntityShinyEnergy.java} 逐字相同）。
 *
 * <p>原版（{@code EntityThrowable}，无重力、尺寸 0×0、{@code onImpact} 空实现——穿过一切方块与实体）：
 * 出生点 = 佩戴者身体中心 + 每轴 {@code (random-0.5)*3.0} 的随机偏移，初速 = 该偏移取反归一化 × 0.1；
 * 每 tick 在自身 ±0.05 内发 8 颗 sparkle（{@code r=0.9+rand*0.1、g=0.2+rand*0.2、b=0}，
 * 尺寸 {@code min(1/距离, 1.5)}），然后把速度<b>覆写</b>成「朝目标中心、大小 0.15」；
 * {@code ticksExisted > 30} 或 {@code target == null} 就消失；服务端每 tick 检查目标是否落进
 * 自身包围盒外扩 0.1 的盒子，是就消失。
 *
 * <p>1.21.1 对应：
 * <ul>
 *   <li><b>基类换成 {@link Entity}</b>：既不需要重力也不需要碰撞，直接手动前进，省掉一整套投射物碰撞检测；</li>
 *   <li><b>目标用 {@link SynchedEntityData} 同步</b>（原版写进 {@code IEntityAdditionalSpawnData} 生成包）；
 *       两端都能解析出目标，客户端因此可以自己算 {@code size} 并本地移动；</li>
 *   <li><b>sparkle 直接用 Botania 的现代对应物</b> {@link FRParticles#sparkle}：颜色、单颗尺寸、
 *       位置抖动 ±0.05 全部按原版公式算；只有数量按玩家反馈砍到 25%
 *       （见 {@link #SPARKLES_PER_TICK}，想恢复原版改回 8 即可）；</li>
 *   <li><b>不做撞击爆发</b>：原版的 {@code particleExplosion()}（24 颗 wisp）在 1.7.10 与 RE 里
 *       <b>都没有任何调用点</b>，是死代码。本项目早期一度把它接到「到达施法者」上，但实测整屏青绿气泡、
 *       原版根本不会出现这一幕，因此现在<b>保持原版原样、不调用</b>，能量体到达时直接消失；</li>
 *   <li>到达判定用「自身包围盒外扩 0.1 与目标包围盒相交」，等价于原版的
 *       {@code getEntitiesWithinAABB(..., box.expand(0.1,0.1,0.1)).contains(target)}。</li>
 * </ul>
 *
 * <p>性能：原版的 8 颗 sparkle 就是<b>客户端</b>发的，这里保持一致——粒子只在客户端 {@code tick()} 里
 * {@code addParticle}，不走网络、不占带宽。
 */
public class EntityShinyEnergy extends Entity {

    /** 追踪目标（佩戴者）的实体 id，-1 表示无目标。 */
    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.INT);

    /** 原版每 tick 朝目标的速度（直接覆写，不是加速度）。 */
    private static final double SPEED = 0.15D;

    /** 原版 {@code ticksExisted > 30} 就消失。 */
    private static final int MAX_LIFE_TICKS = 30;

    /**
     * 每 tick 的 sparkle 数量。
     *
     * <p>原版是 <b>8</b>；玩家实测反馈日耀石的粒子太密集（档位高时 4 tick 生成 4 个能量体、
     * 每个活 30 tick，叠起来整屏都是），要求「保留 25%」，所以这里取 {@code 8 * 0.25 = 2}。
     * 想恢复原版密度把这里的 2 改回 8 即可。
     */
    private static final int SPARKLES_PER_TICK = 2;

    /** 原版到达判定的外扩量。 */
    private static final double REACH_INFLATE = 0.1D;

    public EntityShinyEnergy(EntityType<? extends EntityShinyEnergy> type, Level level) {
        super(type, level);
    }

    /** 由日耀石生成：出生点与初速由物品侧设置（原版 {@code spawnEnergyParticle}）。 */
    public EntityShinyEnergy(Level level, LivingEntity target) {
        super(FREntities.SHINY_ENERGY.get(), level);
        setTarget(target);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, -1);
    }

    public void setTarget(LivingEntity target) {
        entityData.set(DATA_TARGET, target == null ? -1 : target.getId());
    }

    /** 解析同步过来的目标；目标已卸载或已死时返回 {@code null}（与原版「target == null 就消失」一致）。 */
    private LivingEntity resolvedTarget() {
        int id = entityData.get(DATA_TARGET);
        return id >= 0 && level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > MAX_LIFE_TICKS) {
            discard();
            return;
        }
        var target = resolvedTarget();
        if (target == null) {
            discard();
            return;
        }

        // 原版先把速度覆写成「朝目标中心 0.15」，由 EntityThrowable 在两端各自做本地移动。
        // 这里基类是裸 Entity（不会自己移动），所以算完速度后手动前进一格。
        var center = centerOf(this);
        var toTarget = centerOf(target).subtract(center);
        if (toTarget.lengthSqr() > 1.0E-8D) {
            var motion = toTarget.normalize().scale(SPEED);
            setDeltaMovement(motion);
            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        }

        if (level().isClientSide()) {
            spawnSparkles(target, center.distanceTo(centerOf(target)));
            return;
        }
        // 服务端：目标进入自身外扩 0.1 的盒子就消失。原版这里同样只 setDead，没有任何粒子。
        if (getBoundingBox().inflate(REACH_INFLATE).intersects(target.getBoundingBox())) {
            discard();
        }
    }

    /**
     * 原版那 8 颗 sparkle：
     * {@code sparkleFX(x±0.05, y±0.05, z±0.05, 0.9+rand*0.1, 0.2+rand*0.2, 0, min(1/dist, 1.5), 2)}。
     */
    private void spawnSparkles(LivingEntity target, double distance) {
        float size = distance < 1.0E-4D ? 1.5F : (float) Math.min(1.0D / distance, 1.5D);
        for (int i = 0; i < SPARKLES_PER_TICK; i++) {
            float r = (float) (0.9D + random.nextDouble() * 0.1D);
            float g = (float) (0.2D + random.nextDouble() * 0.2D);
            FRParticles.sparkle(level(),
                    getX() + (random.nextDouble() - 0.5D) * 0.1D,
                    getY() + (random.nextDouble() - 0.5D) * 0.1D,
                    getZ() + (random.nextDouble() - 0.5D) * 0.1D,
                    r, g, 0.0F, size, 2);
        }
    }

    /** 对应原版/RE 的 {@code Vector3.fromEntityCenter}：脚底坐标 + 身高的一半。 */
    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    /** 目标不落盘：原版的 target 字段同样不写 NBT，重载后自然消失（两个方法都留空）。 */
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
