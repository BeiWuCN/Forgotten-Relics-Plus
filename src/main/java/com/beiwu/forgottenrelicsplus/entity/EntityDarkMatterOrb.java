package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityDarkMatterOrb extends FRHomingProjectile {

    private static final int SELF_DESTRUCT_TICK = 100;

    private static final int MAX_LIFE_TICKS = 199;

    private static final double EXPLOSION_RADIUS = 1.0D;

    private static final double STOPPED_EPSILON = 0.01D;

    public EntityDarkMatterOrb(EntityType<? extends EntityDarkMatterOrb> type, Level level) {
        super(type, level);
    }

    public EntityDarkMatterOrb(Level level, LivingEntity shooter) {
        super(FREntities.DARK_MATTER_ORB.get(), level);
        setOwner(shooter);
        Vec3 look = shooter.getLookAngle();
        Vec3 origin = shooter.position()
                .add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.0D));
        setPos(origin.x, origin.y + 0.5D, origin.z);
        setDeltaMovement(look.scale(1.5D));
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

        Vec3 motion = getDeltaMovement();
        if (tickCount >= SELF_DESTRUCT_TICK
                && Math.abs(motion.x) < STOPPED_EPSILON
                && Math.abs(motion.y) < STOPPED_EPSILON
                && Math.abs(motion.z) < STOPPED_EPSILON) {
            detonate();
            discard();
        }
    }

    @Override
    protected void spawnTrailParticles() {
        for (int i = 0; i < 2; i++) {
            FRParticles.wisp(level(),
                    getX() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    getY() + 0.22D * getBbHeight() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    getZ() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    0.05F, 0.05F, 0.1F, 0.25F,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    0.6F);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && super.canHitEntity(target);
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
        if (result instanceof BlockHitResult) {
            fizzle();
        } else {
            detonate();
        }
    }

    private void detonate() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }
        Entity owner = getOwner();

        if (owner != null) {
            boolean outerLands = level().dimension() == OuterLands.DIMENSION;
            DamageSource source = FRDamageTypes.source(level(), FRDamageTypes.DARK_MATTER, owner);
            float damage = outerLands
                    ? FRConfig.ELDRITCH_SPELL_DAMAGE_EX.get().floatValue()
                    : FRConfig.ELDRITCH_SPELL_DAMAGE.get().floatValue();
            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(EXPLOSION_RADIUS), target -> target != owner && target.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(source, damage);
                applyDebuffs(target, outerLands);
            }
        }
        fizzle();
    }

    private static void applyDebuffs(LivingEntity target, boolean outerLands) {
        if (outerLands) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 2));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 250, 3));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
        }
    }

    private static boolean isPenetrable(BlockState state) {
        return state.getBlock() instanceof BushBlock
                || state.is(BlockTags.LEAVES)
                || !state.getFluidState().isEmpty();
    }

    private void fizzle() {
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.NEUTRAL, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        if (level() instanceof ServerLevel server) {

            FRParticles.serverWispBurst(server, getX(), getY(), getZ(),
                    0.1F, 0.1F, 0.15F, 0.25F, 0.8F, 30, 0.3D, 0.043D);

            FRParticles.serverSparkle(server, getX(), getY(), getZ(), 0.4F, 0.4F, 0.6F, 2.0F, 4);
        }
    }
}
