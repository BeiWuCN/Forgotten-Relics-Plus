package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.RandomSelection;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityThunderpealOrb extends FRHomingProjectile {

    private static final double BLAST_RADIUS = 6.0D;

    private static final double CHAIN_RADIUS = 4.0D;
    private static final int MAX_CHAIN_TARGETS = 3;

    private static final int MAX_LIFE_TICKS = 500;

    public EntityThunderpealOrb(EntityType<? extends EntityThunderpealOrb> type, Level level) {
        super(type, level);
    }

    public EntityThunderpealOrb(Level level, LivingEntity shooter) {
        super(FREntities.THUNDERPEAL_ORB.get(), level);
        setOwner(shooter);
        Vec3 look = shooter.getLookAngle();
        Vec3 spawn = shooter.position()
                .add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.25D))
                .add(0.0D, 0.5D, 0.0D);
        setPos(spawn.x, spawn.y, spawn.z);
        setDeltaMovement(look.scale(1.5D));
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    protected void spawnTrailParticles() {
        FRParticles.wisp(
                level(),
                getX(),
                getY(),
                getZ(),
                random.nextFloat() * 0.1F,
                0.4F + random.nextFloat() * 0.3F,
                0.9F + random.nextFloat() * 0.1F,
                0.12F + random.nextFloat() * 0.08F,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                0.6F);
        if (tickCount % 2 == 0) {
            FRParticles.sparkle(level(), getX(), getY(), getZ(), 0.4F, 0.6F, 1.0F, 1.0F, 2);
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
        SoundHelper.play(
                level(),
                getX(),
                getY(),
                getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                1.0F,
                1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        return true;
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        DamageSource lightning = FRDamageTypes.source(level(), FRDamageTypes.TRUE_LIGHTNING, this);
        Player owner = getOwner() instanceof Player player ? player : null;

        Vec3 origin = position();

        LivingEntity directHit = null;
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity direct) {
            strike(direct, lightning, FRConfig.THUNDERPEAL_DIRECT_DAMAGE.get().floatValue());
            directHit = direct;
        }
        final LivingEntity excludeDirect = directHit;

        List<LivingEntity> nearby = level().getEntitiesOfClass(
                        LivingEntity.class,
                        getBoundingBox().inflate(BLAST_RADIUS),
                        entity -> entity != owner && entity != excludeDirect && entity.isAlive());
        AABB chainSearch = getBoundingBox();
        for (LivingEntity target : nearby) {
            chainSearch = chainSearch.minmax(target.getBoundingBox().inflate(CHAIN_RADIUS));
        }
        List<LivingEntity> chainPool = nearby.isEmpty()
                ? List.of()
                : level().getEntitiesOfClass(
                                LivingEntity.class, chainSearch, entity -> entity != owner && entity.isAlive());
        for (LivingEntity target : nearby) {

            FRBoltParticleData.broadcast(
                    server,
                    origin,
                    centerOf(target),
                    FRBoltParticleData.WIDTH_MAIN,
                    1,
                    FRBoltParticleData.ARC_RED,
                    FRBoltParticleData.ARC_GREEN,
                    FRBoltParticleData.ARC_BLUE);
            strike(target, lightning, FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue());
            AABB area = target.getBoundingBox().inflate(CHAIN_RADIUS);
            List<LivingEntity> candidates = new ArrayList<>();
            for (LivingEntity candidate : chainPool) {
                if (candidate != target
                        && candidate.isAlive()
                        && candidate.getBoundingBox().intersects(area)) {
                    candidates.add(candidate);
                }
            }
            List<LivingEntity> chained = RandomSelection.sample(candidates, MAX_CHAIN_TARGETS, random::nextInt);
            for (LivingEntity secondary : chained) {

                FRBoltParticleData.broadcast(
                        server,
                        centerOf(target),
                        centerOf(secondary),
                        FRBoltParticleData.WIDTH_CHAIN,
                        1,
                        FRBoltParticleData.ARC_RED,
                        FRBoltParticleData.ARC_GREEN,
                        FRBoltParticleData.ARC_BLUE);
                strike(
                        secondary,
                        lightning,
                        FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue() / 2.0F);
            }
        }

        for (int i = 0; i < 2; i++) {
            FRParticles.serverWispBurst(
                    server,
                    getX(),
                    getY(),
                    getZ(),
                    0.0F,
                    (float) (0.8D + random.nextDouble() * 0.2D),
                    (float) (0.4D + random.nextDouble() * 0.6D),
                    2.0F,
                    1.0F,
                    1,
                    0.0D,
                    0.0D);
        }
        SoundHelper.play(
                level(),
                getX(),
                getY(),
                getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }

    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static void strike(LivingEntity target, DamageSource source, float damage) {
        target.invulnerableTime = 0;
        target.hurt(source, damage);
    }
}
