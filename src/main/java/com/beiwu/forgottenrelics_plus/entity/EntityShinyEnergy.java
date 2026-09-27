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
 * <h2>原版怎么做</h2>
 * <ul>
 *   <li>{@code EntityThrowable} 子类，{@code getGravityVelocity() = 0}，尺寸 {@code 0×0}；
 *       {@code onImpact} 是<b>空实现</b>——它穿过途中一切方块与实体；</li>
 *   <li>由 {@code ItemShinyStone#spawnEnergyParticle} 生成：出生点 = 佩戴者身体中心
 *       {@code Vector3.fromEntityCenter} 再加一个每轴 {@code (Math.random()-0.5)*3.0} 的随机偏移；
 *       初速 = 该偏移取反归一化后乘 {@code 0.1}；</li>
 *   <li>每 tick：{@code ticksExisted > 30} 就消失；{@code target == null} 也消失；
 *       计算 {@code size = min(1.0 / 到目标的距离, 1.5)}，然后在自身位置 ±0.05 内发
 *       <b>8 颗</b> sparkle（颜色 {@code r = 0.9+rand*0.1、g = 0.2+rand*0.2、b = 0}，
 *       尺寸就是上面那个 {@code size}）；</li>
 *   <li>然后把速度直接改写成「朝目标中心、大小 0.15」（不是加速度，是覆写），
 *       由 {@code EntityThrowable} 自己在两端做本地移动；</li>
 *   <li>服务端每 tick 检查「目标是否落进自身包围盒外扩 0.1 的盒子」，是就消失。</li>
 * </ul>
 *
 * <h2>我们怎么对应（现代写法）</h2>
 * <ul>
 *   <li><b>基类换成 {@link Entity}</b>：原版是 {@code EntityThrowable}，但这里既不需要重力、
 *       也不需要碰撞（{@code onImpact} 本来就是空的），直接用 {@code Entity} 手动前进，
 *       省掉一整套投射物碰撞检测；</li>
 *   <li><b>目标用 {@link SynchedEntityData} 同步</b>：原版靠 {@code IEntityAdditionalSpawnData}
 *       把目标 id 写进生成包，这里换成同步数据（与 {@code FRHomingProjectile} 同一套做法），
 *       两端都能解析出目标，客户端因此可以自己算 {@code size} 并本地移动；</li>
 *   <li><b>sparkle 直接用 Botania</b>：原版调的就是 Botania 的 {@code sparkleFX}，1.21.1 有
 *       现成对应物 {@link FRParticles#sparkle}（内部是 {@code SparkleParticleData}）。sparkle
 *       的颜色、单颗尺寸、位置抖动 ±0.05 全部按原版公式算，不再用原版粒子近似；
 *       只有数量按玩家反馈砍到 25%（见 {@link #SPARKLES_PER_TICK}）；</li>
 *   <li><b>不做撞击爆发</b>：原版的 {@code particleExplosion()}（24 颗 wisp，颜色
 *       {@code (0, 0.8+rand*0.2, 0.4+rand*0.6)}、尺寸 {@code 0.3+rand*0.3}、初速 {@code (rand-0.5)*0.4}）
 *       在 1.7.10 与 RE 里<b>都没有任何调用点</b>，是一段死代码。本项目早期一度把它接到
 *       「到达施法者」这一事件上作为补完，但实测那团 wisp 又大又亮，叠加起来整屏都是青绿色
 *       气泡（玩家反馈「很夸张」），而且原版根本不会出现这一幕；因此现在<b>保持原版原样、不调用</b>，
 *       能量体到达时直接消失；</li>
 *   <li>服务端的到达判定用「自身包围盒外扩 0.1 与目标包围盒相交」，等价于原版的
 *       {@code getEntitiesWithinAABB(..., box.expand(0.1,0.1,0.1)).contains(target)}。</li>
 * </ul>
 *
 * <p>性能：原版每 tick 8 颗 sparkle 是在<b>客户端</b>发的（Botania 的 proxy 就是客户端调用），
 * 这里保持一致——粒子只在客户端 {@code tick()} 里 {@code addParticle}，不走网络、不占带宽。
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
        // 原版 ticksExisted > 30 就 setDead。
        if (tickCount > MAX_LIFE_TICKS) {
            discard();
            return;
        }
        LivingEntity target = resolvedTarget();
        if (target == null) {
            discard();
            return;
        }

        // 原版先把速度覆写成「朝目标中心 0.15」，由 EntityThrowable 在两端各自做本地移动。
        // 这里基类是裸 Entity（不会自己移动），所以算完速度后手动前进一格。
        Vec3 center = centerOf(this);
        Vec3 toTarget = centerOf(target).subtract(center);
        if (toTarget.lengthSqr() > 1.0E-8D) {
            Vec3 motion = toTarget.normalize().scale(SPEED);
            setDeltaMovement(motion);
            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        }

        if (level().isClientSide()) {
            // 原版这一段（8 颗 sparkle）是在客户端 onUpdate 里做的，这里同样只在客户端发。
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
            // 原版 sparkleFX(pos ± 0.05, r, g, 0, size=min(1/dist, 1.5), m=2)。
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

    // ------------------------------------------------------------------
    // 存档
    // ------------------------------------------------------------------

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 目标不落盘：原版的 target 字段同样不写 NBT，重载后自然消失。
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 同上。
    }
}
