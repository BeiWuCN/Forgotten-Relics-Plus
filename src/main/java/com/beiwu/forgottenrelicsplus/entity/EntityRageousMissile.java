package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.beiwu.forgottenrelicsplus.utils.SmoothHoming;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityRageousMissile extends FRHomingProjectile {

    private static final int AIM_UP_TICKS = 30;

    private static final int LIFESPAN_WITHOUT_TARGET = 160;

    private static final double SPEED = 0.5D;

    private static final double STREAM_SPREAD = 0.18D;

    private static final double EVIL_DETONATE_DISTANCE = 1.0D;

    private static final double TRAIL_STEP = 0.05D;

    private static final String TAG_TIME = "time";

    private int time;

    private boolean evil;

    private Vec3 trailStart = Vec3.ZERO;

    private Vec3 streamDirection;

    public EntityRageousMissile(EntityType<? extends EntityRageousMissile> type, Level level) {
        super(type, level);
    }

    public EntityRageousMissile(Level level, LivingEntity shooter, boolean evil) {
        super(FREntities.RAGEOUS_MISSILE.get(), level);
        setOwner(shooter);
        this.evil = evil;
    }

    @Override
    protected int maxLifeTicks() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick() {

        trailStart = center();
        super.tick();
        if (isRemoved()) {
            return;
        }

        boolean client = level().isClientSide();

        if (client) {
            time++;
            return;
        }
        boolean hasTarget = refreshTarget();
        if (!hasTarget && time > LIFESPAN_WITHOUT_TARGET) {
            discard();
            return;
        }

        var thisVec = center();
        var target = resolvedTarget();
        if (target != null) {
            Vec3 diff = center(target).subtract(thisVec);
            Vec3 aim = diff;
            if (time < AIM_UP_TICKS) {

                aim = new Vec3(diff.x, Math.abs(diff.y) + 1.0D, diff.z);
            }
            setDeltaMovement(SmoothHoming.steer(getDeltaMovement(), aim, SPEED));
            if (evil && diff.length() < EVIL_DETONATE_DISTANCE) {
                discard();
                return;
            }
        } else {

            if (streamDirection == null) {
                streamDirection = computeStreamDirection();
            }
            setDeltaMovement(SmoothHoming.steer(getDeltaMovement(), streamDirection.scale(10.0D), SPEED));
        }
        time++;
    }

    private Vec3 computeStreamDirection() {
        Vec3 aim = getOwner() instanceof LivingEntity thrower
                ? thrower.getLookAngle()
                : getDeltaMovement();
        if (aim.lengthSqr() < 1.0E-6D) {
            aim = getDeltaMovement().lengthSqr() > 1.0E-6D
                    ? getDeltaMovement()
                    : new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 dir = aim.add(
                (random.nextDouble() - 0.5D) * STREAM_SPREAD,
                (random.nextDouble() - 0.5D) * STREAM_SPREAD,
                (random.nextDouble() - 0.5D) * STREAM_SPREAD);
        return dir.lengthSqr() < 1.0E-6D ? aim.normalize() : dir.normalize();
    }

    private boolean refreshTarget() {
        return refreshSummonTarget(FRConfig.NUCLEAR_FURY_CLEAR_RANGE.get()) != null;
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide()
                && result instanceof EntityHitResult entityHit
                && entityHit.getEntity() == resolvedTarget()) {
            onImpact(result);
        }
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(result instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;

        if (owner != null) {
            double min = FRConfig.NUCLEAR_FURY_DAMAGE_MIN.get();
            double max = FRConfig.NUCLEAR_FURY_DAMAGE_MAX.get();
            float damage = (float) (min + random.nextDouble() * (max - min));
            target.hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner), damage);
        }

        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                2.0F, 0.8F + random.nextFloat() * 0.2F);
        discard();
    }

    @Override
    protected void spawnTrailParticles() {
        Vec3 to = center();
        Vec3 diff = to.subtract(trailStart);
        double length = diff.length();
        if (length < 1.0E-4D) {
            return;
        }
        int steps = (int) (length / TRAIL_STEP);
        if (steps <= 0) {
            return;
        }
        Vec3 step = diff.scale(TRAIL_STEP / length);
        Vec3 pos = trailStart;
        for (int i = 0; i < steps; i++) {
            addSparkle(pos);
            if (random.nextInt(steps) <= 1) {
                addSparkle(pos.add(
                        (random.nextDouble() - 0.5D) * 0.4D,
                        (random.nextDouble() - 0.5D) * 0.4D,
                        (random.nextDouble() - 0.5D) * 0.4D));
            }
            pos = pos.add(step);
        }

        if (!level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.5D)).isEmpty()) {
            Vec3 origin = center();
            for (int i = 0; i < 12; i++) {
                FRParticles.wisp(level(), origin.x, origin.y, origin.z,
                        0.0F,
                        (float) (0.8D + random.nextDouble() * 0.2D),
                        (float) (0.4D + random.nextDouble() * 0.6D),
                        0.2F + random.nextFloat() * 0.1F,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D);
            }
        }
    }

    private void addSparkle(Vec3 pos) {
        FRParticles.sparkle(level(), pos.x, pos.y, pos.z,
                0.0F,
                (float) (0.8D + random.nextDouble() * 0.2D),
                (float) (0.4D + random.nextDouble() * 0.6D),
                0.8F, 2);
    }

    private Vec3 center() {
        return position().add(0.0D, getBbHeight() / 2.0D, 0.0D);
    }

    private static Vec3 center(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt(TAG_TIME, time);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        time = tag.getInt(TAG_TIME);
    }
}
