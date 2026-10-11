package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemTeleportationTome extends FRItem implements FRRechargable, IWarpingGear {

    private static final double TELEPORT_RANGE = 128.0D;

    private static final double ENTITY_TOLERANCE = 4.0D;

    private static final double FORWARD_DISTANCE = 16.0D;

    private static final int COLUMN_SEARCH = 32;

    private static final int WARP = 2;

    public ItemTeleportationTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.DISCORD_TOME_VIS_COST.get() * FRConfig.DISCORD_TOME_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.DISCORD_TOME_MAX_CHARGE.get()
                : FRConfig.DISCORD_TOME_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return WARP;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return cast(level, player, player.getItemInHand(hand), hand);
    }

    public InteractionResultHolder<ItemStack> castFromRing(Player player, ItemStack stack) {
        return cast(player.level(), player, stack, InteractionHand.MAIN_HAND);
    }

    private InteractionResultHolder<ItemStack> cast(Level level, Player player, ItemStack stack, InteractionHand hand) {

        if (level.dimension() == OuterLands.DIMENSION) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return InteractionResultHolder.fail(stack);
            }
            teleportForward(level, player);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }

        LivingEntity pointed = findPointedEntity(level, player);
        if (pointed != null) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return InteractionResultHolder.fail(stack);
            }
            swapWith(level, player, pointed);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }

        BlockPos landing = findPointedColumnTop(level, player);
        if (landing != null && RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            teleportToColumnTop(level, player, landing);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private static void finish(Player player, InteractionHand hand) {
        CooldownHelper.setCooldown(player, FRConfig.DISCORD_TOME_COOLDOWN.get());
        player.swing(hand, true);
    }

    private static void teleportForward(Level level, Player player) {

        Vec3 from = centerOf(player).add(0.0D, -0.5D, 0.0D);
        Vec3 destination = from.add(player.getViewVector(1.0F).scale(FORWARD_DISTANCE));

        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        teleportEntity(level, player, destination);
        SoundHelper.play(
                level,
                destination.x,
                destination.y,
                destination.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        portalTrace(level, from, destination);
    }

    private static void swapWith(Level level, Player player, LivingEntity target) {
        Vec3 playerCenter = centerOf(player);
        Vec3 targetCenter = centerOf(target);

        teleportEntity(level, player, targetCenter);
        teleportEntity(level, target, playerCenter);

        SoundHelper.play(
                level,
                targetCenter.x,
                targetCenter.y,
                targetCenter.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        SoundHelper.play(
                level,
                playerCenter.x,
                playerCenter.y,
                playerCenter.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        portalTrace(level, playerCenter, targetCenter);
    }

    private static void teleportToColumnTop(Level level, Player player, BlockPos landing) {

        Vec3 from = centerOf(player);
        Vec3 destination = new Vec3(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);

        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        teleportEntity(level, player, destination);

        SoundHelper.play(
                level,
                landing.getX(),
                landing.getY(),
                landing.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        portalTrace(level, from, destination);
    }

    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    private static void teleportEntity(Level level, Entity entity, Vec3 destination) {
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(
                    serverPlayer.serverLevel(),
                    destination.x,
                    destination.y,
                    destination.z,
                    serverPlayer.getYRot(),
                    serverPlayer.getXRot());
        } else {
            entity.teleportTo(destination.x, destination.y, destination.z);
        }
    }

    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(TELEPORT_RANGE));
        AABB search = new AABB(eye, end).inflate(ENTITY_TOLERANCE);
        List<Entity> candidates =
                level.getEntities(player, search, entity -> entity instanceof LivingEntity && entity.isPickable());

        Entity best = null;
        double bestDistance = TELEPORT_RANGE;
        for (Entity candidate : candidates) {
            AABB box = candidate.getBoundingBox().inflate(ENTITY_TOLERANCE);
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
                best = candidate;
            }
        }
        return best instanceof LivingEntity living ? living : null;
    }

    private static BlockPos findPointedColumnTop(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(TELEPORT_RANGE));
        BlockHitResult hit =
                level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos hitPos = hit.getBlockPos();

        for (int counter = 0; counter <= COLUMN_SEARCH; counter++) {
            BlockPos feet = hitPos.offset(0, counter, 0);
            if (isSolidFloor(level, feet.below()) && isPassable(level, feet) && isPassable(level, feet.above())) {
                return feet;
            }
        }
        return null;
    }

    private static boolean isSolidFloor(Level level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isPassable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private static void portalTrace(Level level, Vec3 from, Vec3 to) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int steps = (int) Math.ceil(from.distanceTo(to) * 2.0D);
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : (double) i / (double) steps;
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t,
                    from.z + (to.z - from.z) * t,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
        serverLevel.sendParticles(ParticleTypes.PORTAL, from.x, from.y, from.z, 32, 0.3D, 0.3D, 0.3D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.PORTAL, to.x, to.y, to.z, 32, 0.3D, 0.3D, 0.3D, 0.05D);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemTeleportationTome1.lore"));

        tooltip.add(Component.translatable("item.ItemTeleportationTome2.lore", (int) TELEPORT_RANGE));
        tooltip.add(Component.translatable("item.ItemTeleportationTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemTeleportationTome4.lore"));
        tooltip.add(Component.translatable("item.ItemTeleportationTome5.lore", (int) FORWARD_DISTANCE));
        tooltip.add(Component.translatable("item.ItemTeleportationTome6.lore"));
    }
}
