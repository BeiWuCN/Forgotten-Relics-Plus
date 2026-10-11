package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.utils.ProjectilePositionCorrection;
import com.beiwu.forgottenrelicsplus.utils.ProjectileRenderMotion;
import com.beiwu.forgottenrelicsplus.utils.SmoothHoming;
import com.beiwu.forgottenrelicsplus.utils.SummonTargeting;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class FRHomingProjectile extends ThrowableProjectile {

    private ProjectilePositionCorrection positionCorrection;
    private ProjectileRenderMotion renderMotion;
    private int nextTargetSearch;

    protected LivingEntity refreshSummonTarget(double range) {
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        LivingEntity current = resolvedTarget();
        if (current != null && (!SummonTargeting.isEnemy(current, owner) || distanceToSqr(current) > range * range)) {
            current = null;
            setTarget(null);
        }
        if (tickCount >= nextTargetSearch) {
            nextTargetSearch = tickCount + 10 + Math.floorMod(getId(), 3);
            current = SummonTargeting.select(this, owner, range, current);
            setTarget(current);
        }
        return current;
    }

    public ProjectileRenderMotion renderMotion() {
        return renderMotion;
    }

    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(FRHomingProjectile.class, EntityDataSerializers.INT);

    protected FRHomingProjectile(EntityType<? extends FRHomingProjectile> type, Level level) {
        super(type, level);
    }

    protected double homingStrength() {
        return 0.0D;
    }

    protected double homingSpeed() {
        return 0.35D;
    }

    protected int maxLifeTicks() {
        return 200;
    }

    protected abstract void onImpact(HitResult result);

    public float renderScale() {
        return 1.0F;
    }

    protected void spawnTrailParticles() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, -1);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    public void setTarget(LivingEntity target) {
        entityData.set(DATA_TARGET, target == null ? -1 : target.getId());
    }

    protected LivingEntity resolvedTarget() {
        int id = entityData.get(DATA_TARGET);
        return id >= 0 && level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        if (level().isClientSide() && tickCount > 0) {
            if (positionCorrection == null) positionCorrection = new ProjectilePositionCorrection();
            if (positionCorrection.retarget(x - getX(), y - getY(), z - getZ())) {
                setRot(yaw, pitch);
                return;
            }
        }
        if (positionCorrection != null) positionCorrection.reset();
        super.lerpTo(x, y, z, yaw, pitch, steps);
        if (renderMotion != null) {
            renderMotion.reset(x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void tick() {
        if (level().isClientSide() && renderMotion == null) {
            renderMotion = new ProjectileRenderMotion();
            Vec3 motion = getDeltaMovement();
            renderMotion.reset(getX(), getY(), getZ(), motion.x, motion.y, motion.z);
        }
        super.tick();
        if (isRemoved()) return;
        if (tickCount > maxLifeTicks()) {
            discard();
            return;
        }

        if (level().isClientSide()) {
            if (positionCorrection != null) {
                Vec3 correction = positionCorrection.consume();
                if (correction != Vec3.ZERO) {
                    setPos(getX() + correction.x, getY() + correction.y, getZ() + correction.z);
                }
            }
            renderMotion.advance(getX(), getY(), getZ());
            spawnTrailParticles();
        } else {
            applyHoming();
        }
    }

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
            setDeltaMovement(SmoothHoming.steer(getDeltaMovement(), toTarget, homingSpeed()));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide()) {
            onImpact(result);
            discard();
        }
    }
}
