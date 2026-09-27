package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 千咒之诫的灵魂能量（Soul Energy），1.7.10 原版 {@code EntitySoulEnergy}，
 * 本项目注册名 {@code soul_energy}。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类，{@code getGravityVelocity} 返回 0）：
 * <ul>
 *   <li>无重力飞行，生存时限 1000 tick；</li>
 *   <li>追踪目标（施法者）：「朝目标身高 60% 处」每 tick 叠加一个单位向量的 0.3 倍加速度，
 *       再把三个速度分量各自夹到 ±0.35；目标为空时立即 {@code setDead()}；</li>
 *   <li>{@code onImpact} 是<b>空实现</b>——灵魂能量穿过途中一切方块与实体，不因碰撞消失；</li>
 *   <li>每 tick 检查自身 ±0.5 的 1×1×1 判定框，若里面出现了目标，就播 {@code random.fizz}、
 *       治疗目标 1 点、并给（必然是玩家的）目标补 1 点饥饿，然后消失；</li>
 *   <li>拖尾：客户端从上一 tick 位置到当前位置沿途每隔 0.05 格撒一颗白色 sparkle，
 *       另有约 {@code 2 / steps} 的概率在旁边补一颗；命中时再撒 7 颗白色 wisp。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}：无重力、生存时限、朝目标加速都由它承担，
 *       本类只补上原版额外的「速度分量限幅」与「1×1×1 距离判定」；</li>
 *   <li>原版的 {@code IEntityAdditionalSpawnData}（把目标 id 写进生成包）在 1.21.1 由基类的
 *       {@code SynchedEntityData} 承担，两端都能解析出目标，不必自己写生成包；</li>
 *   <li>原版 Botania 的 {@code sparkleFX} / {@code wispFX}（白色）改成原版粒子
 *       {@link ParticleTypes#END_ROD}（同为白色光点），不需要自定义渲染器；</li>
 *   <li>音效 {@code random.fizz} → {@link SoundEvents#FIRE_EXTINGUISH}
 *       （与邪术之咒、核子之怒同一替代方案），并按项目约定过 {@link SoundHelper#play} 压音量；
 *       发射音 {@code botania:missile} 由物品侧播放，不在本实体里。</li>
 * </ul>
 *
 * <p><b>与原版的两处偏差</b>：
 * <ol>
 *   <li>原版对「目标已死」的实体仍会继续追踪（只判 {@code target != null}），基类的
 *       {@code resolvedTarget()} 对已死目标返回 {@code null}，于是本实体会直接消失。
 *       灵魂能量的目标是施法者本人，施法者死亡时法术早已中断，实际不可辨；</li>
 *   <li>原版 {@code tick} 在客户端也会执行治疗与 {@code setDead}；1.21.1 把结算收敛到服务端，
 *       客户端只负责拖尾粒子，结果一致。</li>
 * </ol>
 */
public class EntitySoulEnergy extends FRHomingProjectile {

    /** 原版每 tick 朝目标叠加的加速度。 */
    private static final double HOMING_STRENGTH = 0.3D;

    /** 原版把速度三分量各自夹在 ±0.35。 */
    private static final double MAX_SPEED = 0.35D;

    /** 生存时限。原版在 {@code ticksExisted > 1000} 时移除，与基类的 {@code >} 判定对齐。 */
    private static final int MAX_LIFE_TICKS = 1000;

    /** 原版判定框：自身 ±0.5 的 1×1×1 立方体。 */
    private static final double REACH_BOX = 0.5D;

    /** 原版命中目标时的回血 / 补饥饿量：{@code heal(1.0)} / {@code addStats(1, 1.0)}。 */
    private static final float HEAL_AMOUNT = 1.0F;
    private static final int FOOD_AMOUNT = 1;
    private static final float FOOD_SATURATION = 1.0F;

    /** 拖尾粒子：沿途每隔 0.05 格一颗（原版 {@code step = normalize(diff).multiply(0.05)}）。 */
    private static final double TRAIL_STEP = 0.05D;

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> type, Level level) {
        super(type, level);
    }

    /**
     * 由千咒之诫生成。
     *
     * <p>{@code shooter} 是<b>被抽取灵魂的受害者</b>（原版就是这么传的），
     * {@code target} 才是施法者；位置与初速由物品的 {@code spawnSoul} 按受害者的位置与视线设置。
     */
    public EntitySoulEnergy(Level level, LivingEntity shooter, LivingEntity target) {
        super(FREntities.SOUL_ENERGY.get(), level);
        setOwner(shooter);
        setTarget(target);
    }

    @Override
    protected double homingStrength() {
        return HOMING_STRENGTH;
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    /**
     * 基类负责飞行、生存时限与朝目标加速；这里补上原版额外的两步——
     * 速度分量限幅，以及「目标进入 1×1×1 判定框就治疗并消失」。
     */
    @Override
    public void tick() {
        super.tick();
        if (isRemoved() || level().isClientSide()) {
            return;
        }
        // 原版每 tick 把三个速度分量各自夹到 ±0.35（基类只叠加加速度，不夹）。
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(
                Mth.clamp(motion.x, -MAX_SPEED, MAX_SPEED),
                Mth.clamp(motion.y, -MAX_SPEED, MAX_SPEED),
                Mth.clamp(motion.z, -MAX_SPEED, MAX_SPEED));

        LivingEntity target = resolvedTarget();
        if (target == null) {
            // 原版 target == null 时直接 setDead()。
            discard();
            return;
        }
        // 原版 world.getEntitiesWithinAABB(EntityLivingBase.class, 自身 ±0.5 的方框).contains(target)。
        AABB reach = new AABB(
                getX() - REACH_BOX, getY() - REACH_BOX, getZ() - REACH_BOX,
                getX() + REACH_BOX, getY() + REACH_BOX, getZ() + REACH_BOX);
        if (level().getEntitiesOfClass(LivingEntity.class, reach).contains(target)) {
            reachTarget(target);
        }
    }

    /** 原版判定框命中目标后的结算：fizz 音 + 治疗 + 补饥饿 + 7 颗白色 wisp，然后消失。 */
    private void reachTarget(LivingEntity target) {
        // 原版 world.playSoundAtEntity(target, "random.fizz", 0.6F, 0.8F + random * 0.2F)。
        SoundHelper.play(level(), target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.6F, 0.8F + random.nextFloat() * 0.2F);
        target.heal(HEAL_AMOUNT);
        // 原版把目标强转成 EntityPlayer 调 FoodStats.addStats(1, 1.0F)；
        // 1.21.1 的 FoodData#eat 语义相同（与虚空吞噬者同一处理），并用 instanceof 保护。
        if (target instanceof Player player) {
            player.getFoodData().eat(FOOD_AMOUNT, FOOD_SATURATION);
        }
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 7, 0.1D, 0.1D, 0.1D, 0.05D);
        }
        discard();
    }

    /**
     * 拖尾：原版从上一 tick 位置到当前位置沿途每隔 0.05 格撒一颗白色 sparkle，
     * 另有约 {@code 2 / steps} 的概率在 ±0.2 的随机偏移处补一颗。
     *
     * <p>只在客户端调用（由基类负责）。
     */
    @Override
    protected void spawnTrailParticles() {
        Vec3 from = new Vec3(xOld, yOld, zOld);
        Vec3 to = position();
        Vec3 diff = to.subtract(from);
        double length = diff.length();
        if (length < 1.0E-4D) {
            return;
        }
        Vec3 step = diff.scale(TRAIL_STEP / length);
        int steps = (int) (length / TRAIL_STEP);
        Vec3 pos = from;
        for (int i = 0; i < steps; i++) {
            level().addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
            if (random.nextInt(steps) <= 1) {
                level().addParticle(ParticleTypes.END_ROD,
                        pos.x + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.y + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.z + (random.nextDouble() - 0.5D) * 0.4D,
                        0.0D, 0.0D, 0.0D);
            }
            pos = pos.add(step);
        }
    }

    /** 原版 {@code onImpact} 为空：灵魂能量穿过途中一切实体。 */
    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    /**
     * 整段覆写基类的 {@code onHit}：基类对任何命中都会结算并 {@code discard()}，
     * 但原版 {@code onImpact} 是空实现，灵魂能量要一路穿到施法者身边，收尾完全由
     * {@link #tick()} 里的距离判定负责。
     */
    @Override
    protected void onHit(HitResult result) {
        // 空实现：既不结算也不销毁。
    }

    /** 本类覆写了 {@code onHit}，这里永远不会被调用；保留空实现以满足基类契约。 */
    @Override
    protected void onImpact(HitResult result) {
    }
}
