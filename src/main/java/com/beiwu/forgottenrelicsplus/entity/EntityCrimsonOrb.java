package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityCrimsonOrb extends FRHomingProjectile {

    private static final double SEARCH_RANGE = 32.0D;

    private static final double HOMING_STRENGTH = 0.3D;

    private static final double MAX_SPEED = 0.25D;

    private static final int MAX_LIFE_TICKS = 1000;

    public EntityCrimsonOrb(EntityType<? extends EntityCrimsonOrb> type, Level level) {
        super(type, level);
    }

    public EntityCrimsonOrb(Level level, LivingEntity caster, LivingEntity target) {
        super(FREntities.CRIMSON_ORB.get(), level);
        setOwner(caster);
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
    protected double homingSpeed() {
        return MAX_SPEED;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            refreshSummonTarget(SEARCH_RANGE);
        }
        super.tick();
    }

    @Override
    protected void spawnTrailParticles() {
        var from = new Vec3(xo, yo, zo);
        Vec3 to = position();
        Vec3 diff = to.subtract(from);
        double length = diff.length();
        if (length < TRAIL_STEP) {
            return;
        }
        int steps = (int) (length / TRAIL_STEP);
        Vec3 step = diff.scale(TRAIL_STEP / length);
        Vec3 pos = from;
        for (int i = 0; i < steps; i++) {
            float r = 0.8F + random.nextFloat() * 0.2F;
            float g = 0.1F + random.nextFloat() * 0.2F;
            float b = random.nextFloat() * 0.1F;
            FRParticles.sparkle(level(), pos.x, pos.y, pos.z, r, g, b, 0.8F, 2);
            if (random.nextInt(steps) <= 1) {
                FRParticles.sparkle(level(),
                        pos.x + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.y + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.z + (random.nextDouble() - 0.5D) * 0.4D,
                        r, g, b, 0.8F, 2);
            }
            pos = pos.add(step);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof BlockHitResult blockHit
                && isPenetrable(level().getBlockState(blockHit.getBlockPos()))) {
            return;
        }
        if (!level().isClientSide()) {
            onImpact(result);
        }
        discard();
    }

    @Override
    protected void onImpact(HitResult result) {
        Entity owner = getOwner();
        if (result instanceof EntityHitResult entityHit) {

            if (owner == null) {
                return;
            }
            double min = FRConfig.CRIMSON_SPELL_DAMAGE_MIN.get();
            double max = FRConfig.CRIMSON_SPELL_DAMAGE_MAX.get();
            float damage = (float) (min + random.nextDouble() * (max - min));
            entityHit.getEntity().hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner), damage);
        }
        shock();
    }

    private void shock() {
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        if (level() instanceof ServerLevel server) {

            for (int i = 0; i < 15; i++) {
                FRParticles.serverWisp(server, getX(), getY(), getZ(),
                        0.8F + random.nextFloat() * 0.2F,
                        0.1F + random.nextFloat() * 0.2F,
                        random.nextFloat() * 0.1F,
                        0.1F + random.nextFloat() * 0.3F,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        0.9F);
            }

            FRParticles.serverSparkle(server, getX(), getY(), getZ(), 1.0F, 0.2F, 0.1F, 2.0F, 4);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        setDeltaMovement(attacker.getLookAngle().scale(0.9D));
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        return true;
    }

    private static final double TRAIL_STEP = 0.05D;

    private static boolean isPenetrable(BlockState state) {
        return state.getBlock() instanceof BushBlock
                || state.is(BlockTags.LEAVES)
                || !state.getFluidState().isEmpty();
    }
}
