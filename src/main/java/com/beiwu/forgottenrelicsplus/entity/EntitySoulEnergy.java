package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
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

public class EntitySoulEnergy extends FRHomingProjectile {

    private static final double HOMING_STRENGTH = 0.3D;

    private static final double MAX_SPEED = 0.35D;

    private static final int MAX_LIFE_TICKS = 1000;

    private static final double REACH_BOX = 0.5D;

    private static final float HEAL_AMOUNT = 1.0F;
    private static final int FOOD_AMOUNT = 1;
    private static final float FOOD_SATURATION = 1.0F;

    private static final double TRAIL_STEP = 0.05D;

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> type, Level level) {
        super(type, level);
    }

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

    @Override
    public void tick() {
        super.tick();
        if (isRemoved() || level().isClientSide()) {
            return;
        }

        var motion = getDeltaMovement();
        setDeltaMovement(
                Mth.clamp(motion.x, -MAX_SPEED, MAX_SPEED),
                Mth.clamp(motion.y, -MAX_SPEED, MAX_SPEED),
                Mth.clamp(motion.z, -MAX_SPEED, MAX_SPEED));

        var target = resolvedTarget();
        if (target == null) {

            discard();
            return;
        }

        AABB reach = new AABB(
                getX() - REACH_BOX, getY() - REACH_BOX, getZ() - REACH_BOX,
                getX() + REACH_BOX, getY() + REACH_BOX, getZ() + REACH_BOX);
        if (!target.isRemoved() && !target.isSpectator() && reach.intersects(target.getBoundingBox())) {
            reachTarget(target);
        }
    }

    private void reachTarget(LivingEntity target) {

        SoundHelper.play(level(), target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.6F, 0.8F + random.nextFloat() * 0.2F);
        target.heal(HEAL_AMOUNT);

        if (target instanceof Player player) {
            player.getFoodData().eat(FOOD_AMOUNT, FOOD_SATURATION);
        }
        if (level() instanceof ServerLevel server) {

            for (int i = 0; i <= 6; i++) {
                FRParticles.serverWisp(server, getX(), getY(), getZ(),
                        1.0F, 1.0F, 1.0F,
                        0.1F + random.nextFloat() * 0.1F,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D);
            }
        }
        discard();
    }

    @Override
    protected void spawnTrailParticles() {
        var from = new Vec3(xOld, yOld, zOld);
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

            FRParticles.sparkle(level(), pos.x, pos.y, pos.z, 1.0F, 1.0F, 1.0F, 0.8F, 2);
            if (random.nextInt(steps) <= 1) {
                FRParticles.sparkle(level(),
                        pos.x + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.y + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.z + (random.nextDouble() - 0.5D) * 0.4D,
                        1.0F, 1.0F, 1.0F, 0.8F, 2);
            }
            pos = pos.add(step);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void onHit(HitResult result) {
    }

    @Override
    protected void onImpact(HitResult result) {
    }
}
