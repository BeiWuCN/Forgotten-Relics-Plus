package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemDevourerOfTheVoid extends FRItem implements IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final float ARC_LIGHTNING_CHANCE = 0.175F;

    public ItemDevourerOfTheVoid(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.DEVOURER_OF_THE_VOID_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {

        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockPos target = findObelisk(level, player);
        if (target == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {

            stack.set(FRDataComponents.DEVOURER_TARGET.get(), target);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        BlockPos target = stack.get(FRDataComponents.DEVOURER_TARGET.get());

        if (level.isClientSide()) {
            if (target != null) {
                obeliskPortals(level, target);
            }
            return;
        }

        ServerLevel server = (ServerLevel) level;
        if (target == null || !isValidTarget(level, player, target)) {

            player.stopUsingItem();
            return;
        }

        if (level.random.nextFloat() <= ARC_LIGHTNING_CHANCE) {
            randomArc(server, target);
        }

        int interval = FRConfig.DEVOURER_OF_THE_VOID_PULSE_INTERVAL.get();

        if (remainingUseDuration <= 0 || remainingUseDuration % interval != 0 || remainingUseDuration == USE_DURATION) {
            return;
        }

        drain(server, player, target);
    }

    private static void drain(ServerLevel level, Player player, BlockPos target) {

        FRBoltParticleData.broadcast(
                level,
                obeliskTop(target),
                playerCenter(player),
                FRBoltParticleData.WIDTH_MAIN,
                4,
                FRBoltParticleData.ARC_RED,
                FRBoltParticleData.ARC_GREEN,
                FRBoltParticleData.ARC_BLUE);

        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                1.0F,
                0.8F);

        player.hurt(level.damageSources().generic(), 0.01F);
        player.heal(FRConfig.DEVOURER_OF_THE_VOID_HEAL.get().floatValue());
        player.getFoodData().eat(FRConfig.DEVOURER_OF_THE_VOID_HUNGER.get(), 1.0F);

        chargeRandomWand(player);
    }

    private static void chargeRandomWand(Player player) {
        List<ResourceKey<IAspect>> primals = TTAspects.PRIMALS;
        ResourceKey<IAspect> aspect = primals.get(player.getRandom().nextInt(primals.size()));
        ItemStack wand = findWandWithRoom(player, aspect);
        if (wand == null) {
            return;
        }
        double mult = FRConfig.DEVOURER_OF_THE_VOID_VIS_MULT.get();
        int amount = (int) ((5.0D + player.getRandom().nextDouble() * 15.0D) * 1.5D * mult);
        WandVisHelper.addVis(wand, aspect, amount, true);
    }

    private static ItemStack findWandWithRoom(Player player, ResourceKey<IAspect> aspect) {
        Inventory inventory = player.getInventory();
        List<ItemStack> candidates = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !(candidate.getItem() instanceof ItemWand)) {
                continue;
            }
            if (WandVisHelper.getVis(candidate, aspect) < WandVisHelper.getMaxVis(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    private static Vec3 obeliskTop(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 2.75D, pos.getZ() + 0.5D);
    }

    private static Vec3 playerCenter(Player player) {
        return player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
    }

    private static void randomArc(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        Vec3 from = new Vec3(x, pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 2.0D, z);
        Vec3 to = new Vec3(
                x + (level.random.nextDouble() - 0.5D) * 4.0D,
                pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 4.0D,
                z + (level.random.nextDouble() - 0.5D) * 4.0D);
        FRBoltParticleData.broadcast(
                level,
                from,
                to,
                0.4F,
                1,
                FRBoltParticleData.OBELISK_ARC_RED,
                FRBoltParticleData.OBELISK_ARC_GREEN,
                FRBoltParticleData.OBELISK_ARC_BLUE);
    }

    private static void obeliskPortals(Level level, BlockPos pos) {
        for (int i = 0; i <= 4; i++) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    pos.getX() + 0.5D,
                    pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 2.0D,
                    pos.getZ() + 0.5D,
                    (level.random.nextDouble() - 0.5D) * 3.0D,
                    (level.random.nextDouble() - 0.5D) * 0.3D,
                    (level.random.nextDouble() - 0.5D) * 3.0D);
        }
    }

    private static BlockPos findObelisk(Level level, Player player) {
        double range = FRConfig.DEVOURER_OF_THE_VOID_RANGE.get();
        int radius = (int) Math.ceil(range);
        double maxSqr = range * range;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).is(com.leclowndu93150.thaumaturge.registry.TTBlocks.ELDRITCH_OBELISK.get())) {
                continue;
            }
            if (player.position().distanceToSqr(pos.getX(), pos.getY() + 2.5D, pos.getZ()) > maxSqr) {
                continue;
            }
            return pos.immutable();
        }
        return null;
    }

    private static boolean isValidTarget(Level level, Player player, BlockPos pos) {
        if (!level.isLoaded(pos)
                || !(level.getBlockEntity(pos)
                        instanceof com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk)) {
            return false;
        }
        double range = FRConfig.DEVOURER_OF_THE_VOID_RANGE.get();
        return player.position().distanceToSqr(pos.getX(), pos.getY() + 2.5D, pos.getZ()) <= range * range;
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid1.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid2.lore", (int)
                FRConfig.DEVOURER_OF_THE_VOID_RANGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid3.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid4.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid5.lore"));
    }
}
