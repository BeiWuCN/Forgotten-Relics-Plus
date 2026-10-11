package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.beiwu.forgottenrelicsplus.utils.SummonTargeting;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

public class EntityBabylonWeapon extends FRHomingProjectile {

    private static final int CHARGE_TICKS = 15;

    private static final int CHARGE_SOUND_INTERVAL = 20;

    private static final double LAUNCH_SPEED = 3.0D;

    private static final double LOOK_RANGE = 64.0D;

    private static final double SWEEP_RADIUS = 2.0D;

    private static final double IMPACT_RADIUS = 3.0D;

    private static final int MAX_LIVE_TICKS = 200;

    private static final double MAX_KNOCKBACK = 1.0D;

    private static final double BOSS_KNOCKBACK_SCALE = 0.5D;

    private static final int IMPACT_PARTICLES = 40;

    private static final int WISP_COLOR = 0xFFFF00;

    private static final int[] VARIETY_COLORS = {
            0xFFFF00, 0xFFE800, 0xFFD700, 0xFFC800, 0xFFB800, 0xFFA800,
            0xFFF000, 0xFFDD00, 0xFFCC00, 0xFFBB00, 0xFFAA00, 0xFF9900
    };

    private static final String TAG_VARIETY = "variety";
    private static final String TAG_CHARGE_TICKS = "chargeTicks";
    private static final String TAG_LIVE_TICKS = "liveTicks";
    private static final String TAG_DELAY = "delay";
    private static final String TAG_ROTATION = "rotation";

    private static final EntityDataAccessor<Integer> DATA_VARIETY =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_LIVE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_DELAY =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_ROTATION =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.FLOAT);

    private boolean launched;

    private Vec3 launchVelocity = Vec3.ZERO;

    public EntityBabylonWeapon(EntityType<? extends EntityBabylonWeapon> type, Level level) {
        super(type, level);
    }

    public EntityBabylonWeapon(Level level, Player shooter) {
        super(FREntities.BABYLON_WEAPON.get(), level);
        setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIETY, 0);
        builder.define(DATA_CHARGE_TICKS, 0);
        builder.define(DATA_LIVE_TICKS, 0);
        builder.define(DATA_DELAY, 0);
        builder.define(DATA_ROTATION, 0.0F);
    }

    public int getVariety() {
        return entityData.get(DATA_VARIETY);
    }

    public void setVariety(int variety) {
        entityData.set(DATA_VARIETY, variety);
    }

    public int getChargeTicks() {
        return entityData.get(DATA_CHARGE_TICKS);
    }

    public void setChargeTicks(int ticks) {
        entityData.set(DATA_CHARGE_TICKS, ticks);
    }

    public int getLiveTicks() {
        return entityData.get(DATA_LIVE_TICKS);
    }

    public void setLiveTicks(int ticks) {
        entityData.set(DATA_LIVE_TICKS, ticks);
    }

    public int getDelay() {
        return entityData.get(DATA_DELAY);
    }

    public void setDelay(int delay) {
        entityData.set(DATA_DELAY, delay);
    }

    public float getRotation() {
        return entityData.get(DATA_ROTATION);
    }

    public void setRotation(float rotation) {
        entityData.set(DATA_ROTATION, rotation);
    }

    @Override
    protected int maxLifeTicks() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick() {

        if (level().isClientSide()) {
            super.tick();
            return;
        }

        if (!(getOwner() instanceof Player player) || !player.isAlive()) {
            discard();
            return;
        }

        super.tick();
        if (isRemoved()) {
            return;
        }

        int liveTime = getLiveTicks();
        int delay = getDelay();
        if (tickCount <= CHARGE_TICKS) {

            setDeltaMovement(Vec3.ZERO);

            setChargeTicks(getChargeTicks() + 1);
            if (random.nextInt(CHARGE_SOUND_INTERVAL) == 0) {
                SoundHelper.play(level(), getX(), getY(), getZ(),
                        BotaniaSounds.TREASURE_WEAPON_SPAWN, SoundSource.PLAYERS, 0.1F,
                        1.0F + random.nextFloat() * 3.0F);
            }
        } else {
            if (liveTime < delay) {
                setDeltaMovement(Vec3.ZERO);
            } else if (liveTime == delay) {
                launch(player);
            }

            if (launched) {
                setDeltaMovement(launchVelocity);
            }
            setLiveTicks(liveTime + 1);

            if (sweepDirectHit()) {
                return;
            }
        }

        if (liveTime > MAX_LIVE_TICKS + delay) {
            discard();
        }
    }

    private void launch(Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_RANGE));
        BlockHitResult hit = level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY, player));
        Vec3 aim;
        LivingEntity target = SummonTargeting.select(player, player, LOOK_RANGE, null);
        if (target != null) {
            aim = target.getBoundingBox().getCenter();
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            aim = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        } else {
            aim = player.getLookAngle().scale(LOOK_RANGE).add(player.position());
        }
        Vec3 motion = aim.subtract(position());
        if (motion.length() > 1.0E-4D) {
            motion = motion.normalize().scale(LAUNCH_SPEED);
        } else {
            motion = Vec3.ZERO;
        }
        launchVelocity = motion;
        launched = true;
        setDeltaMovement(motion);

        SoundHelper.play(level(), getX(), getY(), getZ(),
                BotaniaSounds.TREASURE_WEAPON_ATTACK, SoundSource.PLAYERS, 2.0F,
                0.1F + random.nextFloat() * 3.0F);
    }

    private boolean sweepDirectHit() {
        AABB sweep = new AABB(new Vec3(xo, yo, zo), position()).inflate(SWEEP_RADIUS);
        Entity owner = getOwner();
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, sweep,
                entity -> entity != owner && entity.isAlive());
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.get(0);
        if (owner != null) {
            target.hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner),
                    FRConfig.APOTHEOSIS_DIRECT_DAMAGE.get().floatValue());
        }
        push(target, position());

        explode();
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide()) {
            return;
        }
        if (result instanceof BlockHitResult blockHit && isSoftBlock(blockHit.getBlockPos())) {

            return;
        }
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() == getOwner()) {

            return;
        }
        explode();
    }

    private boolean isSoftBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof LeavesBlock
                || state.getBlock() instanceof LiquidBlock;
    }

    private void explode() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Entity owner = getOwner();

        burst(server, position());
        if (owner != null) {
            DamageSource magic = FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner);
            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(IMPACT_RADIUS),
                    entity -> entity != owner && entity.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(magic, FRConfig.APOTHEOSIS_IMPACT_DAMAGE.get().floatValue());
                push(target, position());
            }
        }

        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 8.0F, 0.8F + random.nextFloat() * 0.2F);

        FRParticles.serverWispBurst(server, getX(), getY(), getZ(),
                0.8F + random.nextFloat() * 0.2F,
                0.8F + random.nextFloat() * 0.2F,
                0.0F,
                0.3F + random.nextFloat() * 0.3F, 1.0F,
                IMPACT_PARTICLES, 0.0D, 0.072D);
        discard();
    }

    @Override
    protected void onImpact(HitResult result) {
        explode();
    }

    private static void burst(ServerLevel level, Vec3 center) {

        FRParticles.serverWispBurst(level, center.x, center.y, center.z,
                0.0F,
                (float) (0.8D + level.random.nextDouble() * 0.2D),
                (float) (0.4D + level.random.nextDouble() * 0.6D),
                1.5F, 1.0F, 1, 0.0D, 0.0D);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                center.x, center.y, center.z, 24, 0.5D, 0.5D, 0.5D, 0.1D);
    }

    @Override
    protected void spawnTrailParticles() {
        if (getLiveTicks() <= getDelay()) {
            return;
        }
        int color = VARIETY_COLORS[Math.floorMod(getVariety(), VARIETY_COLORS.length)];
        FRParticles.wisp(level(), getX(), getY(), getZ(),
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                0.3F, 0.0D, 0.0D, 0.0D);
    }

    private void push(LivingEntity target, Vec3 source) {
        Vec3 diff = center(target).subtract(source);
        double distance = distanceTo(target);
        if (distance > 1.0E-4D) {
            diff = diff.normalize().scale(1.0D / distance);
        } else {
            diff = Vec3.ZERO;
        }
        if (diff.length() > MAX_KNOCKBACK) {
            diff = diff.normalize();
        }
        if (isBoss(target)) {
            diff = diff.scale(BOSS_KNOCKBACK_SCALE);
        }
        target.push(diff.x, diff.y, diff.z);
    }

    private static boolean isBoss(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss;
    }

    private static Vec3 center(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_VARIETY, getVariety());
        tag.putInt(TAG_CHARGE_TICKS, getChargeTicks());
        tag.putInt(TAG_LIVE_TICKS, getLiveTicks());
        tag.putInt(TAG_DELAY, getDelay());
        tag.putFloat(TAG_ROTATION, getRotation());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariety(tag.getInt(TAG_VARIETY));
        setChargeTicks(tag.getInt(TAG_CHARGE_TICKS));
        setLiveTicks(tag.getInt(TAG_LIVE_TICKS));
        setDelay(tag.getInt(TAG_DELAY));
        setRotation(tag.getFloat(TAG_ROTATION));
    }

    private static Vec3 rotateY(Vec3 vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos);
    }

    public static Vec3 rotateAroundY(Vec3 vector, double degrees) {
        return rotateY(vector, degrees);
    }
}
