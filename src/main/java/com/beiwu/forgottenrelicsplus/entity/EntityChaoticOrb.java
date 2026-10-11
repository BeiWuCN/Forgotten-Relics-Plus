package com.beiwu.forgottenrelicsplus.entity;

import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SmoothHoming;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityChaoticOrb extends FRHomingProjectile {

    private static final double GRAVITY = 0.001D;

    private static final double DRAG = 0.5D;

    private static final double BASE_DRAG = 0.99D;

    private static final int WANDER_START_TICK = 20;

    private static final int MAX_LIFE_TICKS = 5000;

    private static final double SEEK_RANGE = 16.0D;

    private static final double SEEK_MAX_SPEED = 0.2D;

    private static final double WANDER_ACCEL = 0.04D;

    private static final int NO_TARGET_FADE_TICKS = 140;

    private static final int FADE_TICKS = 20;

    private static final double TRAVELER_CHANCE = 0.35D;

    private static final double TRAVEL_SPEED = 0.15D;

    private static final double SELFDESTRUCT_CHANCE = 0.15D;

    private static final double EXPLOSION_MAX_EXTRA = 6.0D;

    private static final int[] PRIMAL_COLORS = {
        0xFFFF7E,
        0x3CD4FC,
        0xFF5A01,
        0x56C000,
        0xD5D4EC,
        0x404040,
    };

    private static final EntityDataAccessor<Integer> DATA_COLOR_INDEX =
            SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SEEKER =
            SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.BOOLEAN);

    private Vec3 wanderAcceleration = Vec3.ZERO;
    private Vec3 desiredWanderAcceleration = Vec3.ZERO;


    private boolean everLockedTarget;

    private int noTargetTicks;

    private boolean traveler;

    private Vec3 travelDirection;

    private boolean selfDestruct;

    private static final EntityDataAccessor<Integer> DATA_FADE =
            SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.INT);

    public EntityChaoticOrb(EntityType<? extends EntityChaoticOrb> type, Level level) {
        super(type, level);
        entityData.set(DATA_COLOR_INDEX, random.nextInt(PRIMAL_COLORS.length));
    }

    public EntityChaoticOrb(Level level, LivingEntity caster, boolean seeker) {
        super(FREntities.PRIMAL_ORB.get(), level);
        entityData.set(DATA_SEEKER, seeker);

        this.traveler = !seeker && random.nextDouble() < TRAVELER_CHANCE;
        this.selfDestruct = random.nextDouble() < SELFDESTRUCT_CHANCE;
        setOwner(caster);
        entityData.set(DATA_COLOR_INDEX, random.nextInt(PRIMAL_COLORS.length));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR_INDEX, 0);
        builder.define(DATA_FADE, 0);
        builder.define(DATA_SEEKER, false);
    }

    private boolean isSeeker() {
        return entityData.get(DATA_SEEKER);
    }

    public int getColorIndex() {
        return entityData.get(DATA_COLOR_INDEX);
    }

    public void setColorIndex(int index) {
        entityData.set(DATA_COLOR_INDEX, Math.floorMod(index, PRIMAL_COLORS.length));
    }

    public static int primalColorRgb(int index) {
        return PRIMAL_COLORS[Math.floorMod(index, PRIMAL_COLORS.length)];
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    public float renderScale() {
        int fade = entityData.get(DATA_FADE);
        if (fade <= 0) {
            return 1.0F;
        }
        return Math.max(0.0F, 1.0F - (float) fade / FADE_TICKS);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {

            if (isInWater()) {
                onImpact(new EntityHitResult(this, position()));
                discard();
                return;
            }

            if (tickCount > WANDER_START_TICK) {
                if (isSeeker()) {
                    if (seek()) {
                        everLockedTarget = true;
                    }
                } else if (traveler) {

                    if (travelDirection == null) {
                        travelDirection = randomTravelDirection();
                    }
                    setDeltaMovement(travelDirection.scale(TRAVEL_SPEED));
                } else {

                    wander();
                }
            }

            if (!everLockedTarget && ++noTargetTicks > NO_TARGET_FADE_TICKS) {
                if (selfDestruct) {
                    onImpact(new EntityHitResult(this, position()));
                    discard();
                    return;
                }
                int fade = entityData.get(DATA_FADE) + 1;
                if (fade >= FADE_TICKS) {
                    discard();
                    return;
                }
                entityData.set(DATA_FADE, fade);
            }
        }
        super.tick();
        if (isRemoved()) {
            return;
        }

        if (!isSeeker()) {
            var motion = getDeltaMovement().scale(DRAG / BASE_DRAG);
            setDeltaMovement(motion.x, motion.y - GRAVITY, motion.z);
        }
    }

    private void wander() {
        if (tickCount % 20 == 0) {
            desiredWanderAcceleration = new Vec3(
                    (random.nextDouble() - 0.5D) * WANDER_ACCEL,
                    (random.nextDouble() - 0.5D) * WANDER_ACCEL,
                    (random.nextDouble() - 0.5D) * WANDER_ACCEL);
        }
        wanderAcceleration = wanderAcceleration.lerp(desiredWanderAcceleration, 0.1D);
        setDeltaMovement(getDeltaMovement().add(wanderAcceleration));
    }

    private Vec3 randomTravelDirection() {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        return new Vec3(Math.cos(angle), (random.nextDouble() - 0.5D) * 0.4D, Math.sin(angle)).normalize();
    }

    private boolean seek() {
        LivingEntity target = refreshSummonTarget(SEEK_RANGE);
        if (target == null) {
            return false;
        }
        Vec3 aim = target.getBoundingBox().getCenter().subtract(position());
        setDeltaMovement(SmoothHoming.steer(getDeltaMovement(), aim, SEEK_MAX_SPEED));
        return true;
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }

        boolean blockInWater = result instanceof BlockHitResult && isInWater();
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() != this
                && getOwner() != null) {
            float damage = (float) (1.0D + random.nextDouble() * FRConfig.CHAOS_TOME_DAMAGE_CAP.get());
            entityHit.getEntity().hurt(
                    FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, getOwner()), damage);
        }

        level().explode(null, getX(), getY(), getZ(),
                (float) (1.0D + random.nextDouble() * EXPLOSION_MAX_EXTRA), false, Level.ExplosionInteraction.NONE);
        float specialChance = blockInWater ? 10.0F : 1.0F;
        if (!isSeeker() && random.nextInt(100) <= specialChance) {
            if (random.nextBoolean()) {
                taintSplosion(server);
            } else {
                NodeGenerator.createRandomNodeAt(server, BlockPos.containing(getX(), getY(), getZ()), random,
                        false, false, true,
                        NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
            }
        }
        spawnImpactParticles(server);
    }

    private void taintSplosion(ServerLevel server) {
        int x = Mth.floor(getX());
        int z = Mth.floor(getZ());
        for (int attempt = 0; attempt < 10; attempt++) {
            int xx = x + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            int zz = z + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            if (!random.nextBoolean()) {
                continue;
            }
            int yy = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xx, zz);
            BlockPos target = new BlockPos(xx, yy, zz);
            if (!TaintBiomeManager.taintColumn(server, target)) {
                continue;
            }
            if (server.getBlockState(target).canBeReplaced()
                    && BlockTaintFibre.hasSolidAttachment(server, target)) {
                server.setBlock(target, BlockTaintFibre.stateForWorld(server, target), Block.UPDATE_ALL);
            }
        }
    }

    private void spawnImpactParticles(ServerLevel server) {
        for (int colorStep = 0; colorStep < PRIMAL_COLORS.length; colorStep++) {
            for (int particle = 0; particle < 4; particle++) {
                float fx = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                float fy = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                float fz = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                int rgb = PRIMAL_COLORS[random.nextInt(PRIMAL_COLORS.length)];
                FRParticles.serverWisp(server,
                        getX() + fx, getY() + fy, getZ() + fz,
                        ((rgb >> 16) & 0xFF) / 255.0F,
                        ((rgb >> 8) & 0xFF) / 255.0F,
                        (rgb & 0xFF) / 255.0F,
                        0.2F + random.nextFloat() * 0.3F,
                        fx * 2.0D, fy * 2.0D, fz * 2.0D,
                        0.9F);
            }
        }
    }

    @Override
    protected void spawnTrailParticles() {
        int rgb = primalColorRgb(getColorIndex());
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        FRParticles.wisp(level(), getX(), getY(), getZ(), r, g, b,
                0.15F + random.nextFloat() * 0.1F,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                0.8F);
        if (tickCount % 3 == 0) {
            FRParticles.sparkle(level(), getX(), getY(), getZ(), r, g, b, 1.0F, 3);
        }
    }
}
