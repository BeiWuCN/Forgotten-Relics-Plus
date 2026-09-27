package com.beiwu.forgottenrelics_plus.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 本模组弹射物的共同基类（对应 1.7.10 的 {@code EntityAIProjectileBase} 与各个 {@code EntityXxxOrb}）：
 * 无重力直线飞行、可选目标追踪、生存时限、命中结算钩子。
 *
 * <p>原版用 {@code IEntityAdditionalSpawnData} 把目标 id 写进生成包；这里改用
 * {@link SynchedEntityData} 同步，两端都能解析出实体，不必自己写生成包。
 *
 * <p>1.7.10 没有实体贴图（形体是 {@code ParticleEngine} 的粒子面片）。现在形体交给
 * {@code client/FROrbRenderer}：只用 {@code MultiBufferSource} + 原版 {@code RenderType}、
 * 不碰 GL 状态，满足 {@code client/package-info.java} 的三条硬约束，无 Sodium / Iris 兼容问题。
 */
public abstract class FRHomingProjectile extends ThrowableProjectile {

    /** 追踪目标的实体 id，-1 表示不追踪。 */
    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(FRHomingProjectile.class, EntityDataSerializers.INT);

    protected FRHomingProjectile(EntityType<? extends FRHomingProjectile> type, Level level) {
        super(type, level);
    }

    /** 每 tick 朝目标叠加的加速度；{@code 0} 表示不追踪。 */
    protected double homingStrength() {
        return 0.0D;
    }

    /** 生存时限（tick）。 */
    protected int maxLifeTicks() {
        return 200;
    }

    /** 命中结算（只在服务端调用，调用后本实体已被移除）。 */
    protected abstract void onImpact(HitResult result);

    /**
     * 渲染缩放（默认 1.0）。目前只有 {@code EntityChaoticOrb} 覆写它做「淡出」：
     * 从未锁定过目标的球在 7 秒后线性缩小到 0 再消失。
     */
    public float renderScale() {
        return 1.0F;
    }

    /** 每 tick 的粒子，只在客户端调用。 */
    protected void spawnTrailParticles() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, -1);
    }

    /** 原版这些弹射物都是无重力的。 */
    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    public void setTarget(LivingEntity target) {
        entityData.set(DATA_TARGET, target == null ? -1 : target.getId());
    }

    /** 解析同步过来的目标；目标已卸载或已死时返回 {@code null}。 */
    protected LivingEntity resolvedTarget() {
        int id = entityData.get(DATA_TARGET);
        return id >= 0 && level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > maxLifeTicks()) {
            discard();
            return;
        }
        // 追踪必须在两端都算：只在服务端加加速度的话，客户端速度每 tick 只衰减不补，
        // 位置一路落后、每 10 tick 被位置包硬拉一次，观感就是「弹幕像 PPT」（1.6.2 修正）。
        // 目标 id 已通过 SynchedEntityData 下发，客户端能算出同一份加速度。
        applyHoming();
        if (level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    /** 对应原版 {@code EntityAIProjectileBase} 的追踪：每 tick 朝目标身高的 60% 处叠加加速度。 */
    private void applyHoming() {
        double strength = homingStrength();
        if (strength <= 0.0D) {
            return;
        }
        var target = resolvedTarget();
        if (target == null) {
            return;
        }
        Vec3 toTarget = new Vec3(
                target.getX() - getX(),
                target.getY() + target.getBbHeight() * 0.6D - getY(),
                target.getZ() - getZ());
        double distance = toTarget.length();
        if (distance > 1.0E-4D) {
            setDeltaMovement(getDeltaMovement().add(toTarget.scale(strength / distance)));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide()) {
            onImpact(result);
        }
        discard();
    }
}
