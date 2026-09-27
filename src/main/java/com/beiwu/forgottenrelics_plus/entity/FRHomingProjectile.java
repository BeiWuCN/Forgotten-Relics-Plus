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
 * 本模组弹射物的共同基类。
 *
 * <p>对应 1.7.10 的 {@code EntityAIProjectileBase} 与各个 {@code EntityXxxOrb}：
 * <ul>
 *   <li><b>无重力</b>直线飞行（原版 {@code EntityThrowable#getGravityVelocity} 返回 0）；</li>
 *   <li>可以锁定一个目标并持续向它加速——原版 {@code EntityAIProjectileBase} 的追踪逻辑，
 *       为了兼容 {@code IEntityAdditionalSpawnData} 的写法，这里改用
 *       {@link SynchedEntityData} 同步目标 id，两端都能解析出实体，不必自己写生成包；</li>
 *   <li>有生存时限（原版 200 / 500 / 1000 tick 不等）。</li>
 * </ul>
 *
 * <h2>视觉</h2>
 *
 * <p>1.7.10 的 jar 里没有实体贴图，那些球体当时是用 {@code ParticleEngine} 的粒子面片画出来的。
 * 现在的做法是：<b>逻辑照旧，形体交给 {@code client/FROrbRenderer}</b>——它用一张自带的柔光贴图
 * 按相机朝向画一个公告板四边形，颜色与大小按法球种类给，同时保留各类法球自己的粒子拖尾。
 *
 * <p>这样既让法球真正可见，又满足 {@code client/package-info.java} 的三条硬约束：
 * 只用 {@code MultiBufferSource} 拿 {@code VertexConsumer}、只用原版 {@code RenderType}，
 * 不碰 GL 状态，也就没有 Sodium / Iris 兼容问题。
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
        // 追踪在**两端都算**（1.6.2 修正）：上一版只在服务端加加速度，客户端的速度只会每 tick 衰减，
        // 于是客户端位置一路落后、每 10 tick 被服务端的位置包硬拉一次——玩家看到的「弹幕像 PPT」。
        // 目标 id 本来就通过 SynchedEntityData 下发，客户端完全可以算出同一份加速度；
        // 加上 updateInterval 改成 1（见 FREntities），位置包只做轻微校正，弹道于是连续。
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
        LivingEntity target = resolvedTarget();
        if (target == null) {
            return;
        }
        // 原版是「朝目标中心加速」，这里同样取目标身高的 60% 处作为瞄准点。
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
