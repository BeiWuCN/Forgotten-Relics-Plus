package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityShinyEnergy extends Entity {

    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.INT);

    private static final double SPEED = 0.15D;

    private static final int MAX_LIFE_TICKS = 30;

    private static final int SPARKLES_PER_TICK = 2;

    private static final double REACH_INFLATE = 0.1D;

    public EntityShinyEnergy(EntityType<? extends EntityShinyEnergy> type, Level level) {
        super(type, level);
    }

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

        if (getBoundingBox().inflate(REACH_INFLATE).intersects(target.getBoundingBox())) {
            discard();
        }
    }

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

    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
