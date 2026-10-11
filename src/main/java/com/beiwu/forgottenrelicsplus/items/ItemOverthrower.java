package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class ItemOverthrower extends FRItem implements FRRechargable, IWarpingGear {

    private static final double SEARCH_RANGE = 64.0D;
    private static final double SEARCH_TOLERANCE = 3.0D;

    private static final double COLLISION_BORDER = 0.1D;

    private static final int SOUND_INTERVAL = 10;

    private static final int SLOWNESS_DURATION = 30;
    private static final int SLOWNESS_AMPLIFIER = 2;

    private static final int VIS_INTERVAL = 20;

    private static final double NETHER_SPREAD = 20002.0D;

    private static final int SEARCH_TOP = 124;

    private static final int MAX_ATTEMPTS = 9;

    private static final int IGNITE_ATTEMPTS = 12;

    public ItemOverthrower(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCostPerSecond() {
        return (int) (FRConfig.EDICT_OF_BANISHMENT_VIS_COST.get() * FRConfig.EDICT_OF_BANISHMENT_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.EDICT_OF_BANISHMENT_MAX_CHARGE.get()
                : FRConfig.EDICT_OF_BANISHMENT_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.EDICT_OF_BANISHMENT_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {

        return FRConfig.EDICT_OF_BANISHMENT_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.dimension() == Level.NETHER) {
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = findPointedEntity(level, player);
        if (target == null) {

            if (!level.isClientSide()) {
                stack.remove(FRDataComponents.EDICT_TARGET.get());
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            stack.set(FRDataComponents.EDICT_TARGET.get(), target.getId());
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }
        ServerLevel server = (ServerLevel) level;

        Integer targetId = stack.get(FRDataComponents.EDICT_TARGET.get());
        if (targetId == null) {

            player.stopUsingItem();
            return;
        }

        int duration = getUseDuration(stack, player);
        int elapsed = duration - remainingUseDuration;
        if (elapsed % VIS_INTERVAL == 0 && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            stack.remove(FRDataComponents.EDICT_TARGET.get());
            player.stopUsingItem();
            return;
        }

        Entity targetEntity = server.getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {

            stack.remove(FRDataComponents.EDICT_TARGET.get());
            player.stopUsingItem();
            return;
        }

        if (remainingUseDuration % SOUND_INTERVAL == 0 && remainingUseDuration != duration) {
            SoundHelper.play(
                    level,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.33F,
                    2.0F);
            SoundHelper.play(
                    level,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.33F,
                    2.0F);
        }

        Vec3 center = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);

        banishingParticles(server, center);

        target.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, true, true));

        if (remainingUseDuration != 1) {
            return;
        }

        if (target.level().dimension() != Level.NETHER) {
            boolean banished = false;

            for (int attempt = MAX_ATTEMPTS - 1; attempt >= 0; attempt--) {
                if (overthrow(server, target, player)) {
                    banished = true;
                    break;
                }
            }

            if (!banished && !(target instanceof ServerPlayer)) {
                target.teleportTo(0.0D, 0.0D, 0.0D);
                target.discard();
            }
        }

        strikeLightning(server, center, target);
        infernalBurst(server, center);

        stack.remove(FRDataComponents.EDICT_TARGET.get());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {

        if (!level.isClientSide()) {
            stack.remove(FRDataComponents.EDICT_TARGET.get());
        }
    }

    private static boolean overthrow(ServerLevel sourceLevel, LivingEntity entity, Player overthrower) {
        ServerLevel nether = sourceLevel.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            return false;
        }

        int x = (int) ((nether.random.nextDouble() - 0.5D) * NETHER_SPREAD);
        int z = (int) ((nether.random.nextDouble() - 0.5D) * NETHER_SPREAD);

        nether.getChunk(x >> 4, z >> 4);

        int y = SEARCH_TOP;
        for (int counter = SEARCH_TOP; counter > 0; counter--) {
            if (!validatePosition(nether, x, counter, z)) {
                continue;
            }
            y = counter;
            break;
        }
        if (y == SEARCH_TOP) {
            return false;
        }

        if (entity instanceof ServerPlayer serverPlayer) {

            serverPlayer.teleportTo(nether, x + 0.5D, y, z + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
            broadcastOverthrow(overthrower, serverPlayer);
            ForgottenRelics.LOGGER.info(
                    "{} has overthrown {} into the Nether.",
                    overthrower.getDisplayName().getString(),
                    serverPlayer.getDisplayName().getString());
            return true;
        }

        Vec3 origin = entity.position();
        Vec3 eye = entity.getEyePosition();
        boolean moved = entity.teleportTo(
                nether, x + 0.5D, y, z + 0.5D, Set.<RelativeMovement>of(), entity.getYRot(), entity.getXRot());
        if (!moved) {

            entity.discard();
        }
        igniteAround(sourceLevel, origin, eye);
        return true;
    }

    private static boolean validatePosition(ServerLevel level, int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState belowState = level.getBlockState(below);
        return !belowState.isAir()
                && belowState.isSolidRender(level, below)
                && level.getBlockState(new BlockPos(x, y, z)).isAir()
                && level.getBlockState(new BlockPos(x, y + 1, z)).isAir();
    }

    private static void igniteAround(ServerLevel level, Vec3 origin, Vec3 eye) {
        BlockPos base = BlockPos.containing(origin);
        for (int attempt = 0; attempt < IGNITE_ATTEMPTS; attempt++) {
            int xx = base.getX() + level.random.nextInt(4) - level.random.nextInt(4);
            int zz = base.getZ() + level.random.nextInt(4) - level.random.nextInt(4);
            int yy;
            for (yy = base.getY() + 4;
                    level.getBlockState(new BlockPos(xx, yy, zz)).isAir() && yy > base.getY() - 4;
                    yy--) {}
            BlockPos ground = new BlockPos(xx, yy, zz);
            BlockPos firePos = ground.above();
            if (!level.getBlockState(firePos).isAir()
                    || level.getBlockState(ground).isAir()
                    || level.getBlockState(firePos).is(Blocks.FIRE)
                    || !canEntityBeSeen(level, eye, firePos)) {
                continue;
            }

            level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    private static boolean canEntityBeSeen(ServerLevel level, Vec3 eye, BlockPos pos) {
        Vec3 target = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);

        BlockHitResult hit = level.clip(new ClipContext(
                eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(SEARCH_RANGE));
        AABB search = new AABB(eye, end).inflate(SEARCH_TOLERANCE);
        LivingEntity best = null;
        double bestDistance = SEARCH_RANGE;
        for (Entity candidate :
                level.getEntities(player, search, entity -> entity instanceof LivingEntity && entity.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(COLLISION_BORDER);
            Optional<Vec3> hit = box.clip(eye, end);
            double distance;
            if (hit.isPresent()) {
                distance = eye.distanceTo(hit.get());
            } else if (box.contains(eye)) {
                distance = 0.0D;
            } else {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) candidate;
            }
        }
        return best;
    }

    private static void banishingParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i < 5; i++) {
            double px = center.x + (level.random.nextDouble() - 0.5D) * 8.0D;
            double py = center.y + (level.random.nextDouble() - 0.5D) * 8.0D;
            double pz = center.z + (level.random.nextDouble() - 0.5D) * 8.0D;
            FRParticles.serverWisp(
                    level,
                    px,
                    py,
                    pz,
                    0.9F + level.random.nextFloat() * 0.1F,
                    0.1F + level.random.nextFloat() * 0.15F,
                    0.0F,
                    0.2F + level.random.nextFloat() * 0.2F,
                    (center.x - px) * 0.08D,
                    (center.y - py) * 0.08D,
                    (center.z - pz) * 0.08D,
                    0.5F);
        }
    }

    private static void infernalBurst(ServerLevel level, Vec3 center) {
        FRParticles.serverWispBurst(
                level,
                center.x,
                center.y,
                center.z,
                0.9F + level.random.nextFloat() * 0.1F,
                0.1F + level.random.nextFloat() * 0.15F,
                0.0F,
                0.4F + level.random.nextFloat() * 0.4F,
                1.0F,
                129,
                0.0D,
                0.144D);
    }

    private static void strikeLightning(ServerLevel level, Vec3 center, LivingEntity target) {
        double y = center.y - target.getBbHeight() / 2.0D;
        for (int counter = 3; counter > 0; counter--) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                continue;
            }
            bolt.moveTo(center.x - 0.5D, y, center.z - 0.5D);
            level.addFreshEntity(bolt);
        }
    }

    private static void broadcastOverthrow(Player overthrower, ServerPlayer victim) {
        MinecraftServer server = overthrower.getServer();
        if (server == null) {
            return;
        }
        Component message = Component.literal(overthrower.getDisplayName().getString() + " ")
                .append(Component.translatable("message.overthrown1"))
                .append(Component.literal(" " + victim.getDisplayName().getString() + " "))
                .append(Component.translatable("message.overthrown2"));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemOverthrower1.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower3.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower5.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower6.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower7.lore"));
    }
}
