package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.registry.FRSounds;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityLunarFlare extends FRHomingProjectile {

    private static final int MAX_LIFE_TICKS = 1000;

    private static final double IMPACT_RADIUS = 2.5D;

    private static final double MAX_KNOCKBACK = 1.0D;

    private static final double BOSS_KNOCKBACK_SCALE = 0.5D;

    private static final double TRAIL_STEP = 0.05D;

    private int lockX;
    private int lockY;
    private int lockZ;

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> type, Level level) {
        super(type, level);
    }

    public EntityLunarFlare(Level level, LivingEntity shooter, int x, int y, int z) {
        super(FREntities.LUNAR_FLARE.get(), level);
        setOwner(shooter);
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Entity owner = getOwner();

        if (owner == null) {
            return;
        }
        DamageSource magic = FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner);

        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() != owner
                && entityHit.getEntity() instanceof LivingEntity direct) {
            direct.hurt(magic, FRConfig.TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE.get().floatValue());
        }

        if (result instanceof BlockHitResult blockHit && isLockedBlock(blockHit.getBlockPos())) {
            explode(server, owner, magic);
        }
    }

    @Override
    protected void onHit(HitResult result) {

        if (level().isClientSide()) {
            return;
        }
        onImpact(result);

        if (!(result instanceof EntityHitResult) || getOwner() == null) {
            discard();
        }
    }

    private void explode(ServerLevel server, Entity owner, DamageSource magic) {
        List<LivingEntity> affected = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(IMPACT_RADIUS),
                entity -> entity.isAlive() && entity != owner);
        Vec3 center = position();
        for (LivingEntity target : affected) {
            target.hurt(magic, FRConfig.TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE.get().floatValue());

            Vec3 push = target.position().subtract(center);
            double distance = distanceTo(target);
            if (distance > 1.0E-4D) {
                push = push.normalize().scale(1.0D / distance);
            } else {
                push = Vec3.ZERO;
            }
            if (push.length() > MAX_KNOCKBACK) {
                push = push.normalize();
            }
            if (isBoss(target)) {
                push = push.scale(BOSS_KNOCKBACK_SCALE);
            }
            target.push(push.x, push.y, push.z);
        }

        FRParticles.serverWispBurst(server, lockX + 0.5D, lockY + 1.25D, lockZ + 0.5D,
                0.0F,
                (float) (0.8D + random.nextDouble() * 0.2D),
                (float) (0.4D + random.nextDouble() * 0.6D),
                0.3F + random.nextFloat() * 0.3F, 1.0F, 49, 0.0D, 0.116D);

        BlockPos lockPos = new BlockPos(lockX, lockY, lockZ);
        BlockState state = level().getBlockState(lockPos);
        server.levelEvent(2001, lockPos, Block.getId(state));

        SoundHelper.play(level(), lockX, lockY, lockZ, FRSounds.LUNAR_FLARE.get(),
                SoundSource.PLAYERS, 16.0F, 0.8F + random.nextFloat() * 0.2F);
        discard();
    }

    private boolean isLockedBlock(BlockPos pos) {
        return pos.getX() == lockX && pos.getY() == lockY && pos.getZ() == lockZ;
    }

    private static boolean isBoss(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss;
    }

    @Override
    protected void spawnTrailParticles() {
        var previous = new Vec3(xo, yo, zo);
        Vec3 current = position();
        Vec3 diff = current.subtract(previous);
        double length = diff.length();
        if (length < TRAIL_STEP) {
            return;
        }
        Vec3 step = diff.normalize().scale(TRAIL_STEP);
        int steps = (int) (length / TRAIL_STEP);
        Vec3 particlePos = previous;
        for (int i = 0; i < steps; i++) {

            float sparkleG = (float) (0.8D + random.nextDouble() * 0.2D);
            float sparkleB = (float) (0.4D + random.nextDouble() * 0.6D);
            FRParticles.sparkle(level(),
                    particlePos.x + (random.nextDouble() - 0.5D) * 0.2D,
                    particlePos.y + (random.nextDouble() - 0.5D) * 0.2D,
                    particlePos.z + (random.nextDouble() - 0.5D) * 0.2D,
                    0.0F, sparkleG, sparkleB, 2.0F, 1);

            if (random.nextInt(steps) <= 1) {
                FRParticles.sparkle(level(),
                        particlePos.x + (random.nextDouble() - 0.5D) * 1.0D,
                        particlePos.y + (random.nextDouble() - 0.5D) * 1.0D,
                        particlePos.z + (random.nextDouble() - 0.5D) * 1.0D,
                        0.0F,
                        (float) (0.8D + random.nextDouble() * 0.2D),
                        (float) (0.4D + random.nextDouble() * 0.6D),
                        2.4F, 4);
            }
            particlePos = particlePos.add(step);
        }
    }
}
