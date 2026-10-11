package com.beiwu.forgottenrelicsplus.entity;

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

    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(FRHomingProjectile.class, EntityDataSerializers.INT);

    protected FRHomingProjectile(EntityType<? extends FRHomingProjectile> type, Level level) {
        super(type, level);
    }

    protected double homingStrength() {
        return 0.0D;
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
    public void tick() {
        super.tick();
        if (tickCount > maxLifeTicks()) {
            discard();
            return;
        }

        applyHoming();
        if (level().isClientSide()) {
            spawnTrailParticles();
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
