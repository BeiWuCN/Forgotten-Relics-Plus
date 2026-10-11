package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.CurioHelper;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class ItemArcanum extends FRCurioItem
        implements FRRechargable, IVisDiscountGear, WearerTickBehaviour, IncomingDamageBehaviour {

    private static final double BASE_GEN_CHANCE = 0.025D;

    public ItemArcanum(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded() ? FRConfig.ARCANUM_MAX_CHARGE.get() : FRConfig.ARCANUM_MAX_CHARGE.getDefault();
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return FRConfig.ARCANUM_VIS_DISCOUNT.get().intValue();
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (!(wearer instanceof ServerPlayer player)) {
            return;
        }

        if (player.getRandom().nextDouble() <= BASE_GEN_CHANCE * FRConfig.ARCANUM_GEN_RATE.get()) {
            if (rechargeOne(player)) {
                return;
            }
        }

        if (player.getRandom().nextDouble() <= FRConfig.ARCANUM_TELEPORT_CHANCE.get()) {
            randomTeleport(player, FRConfig.ARCANUM_TELEPORT_RANGE.get());
            return;
        }

        if (player.getRandom().nextDouble() <= FRConfig.ARCANUM_DORMANT_TRANSFORM_CHANCE.get()) {
            int min = FRConfig.ARCANUM_DORMANT_LIFE_MIN.get();
            int max = FRConfig.ARCANUM_DORMANT_LIFE_MAX.get();
            int span = Math.max(1, max - min);
            int lifetime = (int) ((min + player.getRandom().nextInt(span)) * FRConfig.DORMANT_ARCANUM_VIS_MULT.get());
            ItemStack dormant = new ItemStack(FRItems.DORMANT_ARCANUM.get());
            dormant.set(FRDataComponents.DORMANT_LIFETIME.get(), Math.max(0, lifetime));
            CurioHelper.replaceFirst(player, FRItems.ARCANUM.get(), dormant);
        }
    }

    public static boolean tryDodgeTeleport(ServerPlayer player) {
        return randomTeleport(player, 16);
    }

    private static boolean rechargeOne(ServerPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (tryRecharge(player, player.getInventory().items.get(slot))) {
                return true;
            }
        }
        var handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler != null) {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                if (tryRecharge(player, equipped.getStackInSlot(slot))) {
                    return true;
                }
            }
        }
        for (ItemStack armor : player.getInventory().armor) {
            if (tryRecharge(player, armor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryRecharge(ServerPlayer player, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        return RechargeAccess.rechargeItem(player.serverLevel(), candidate, player.blockPosition(), player, 1) > 0.0F;
    }

    private static boolean randomTeleport(ServerPlayer player, int range) {
        if (range <= 0) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        for (int attempt = 0; attempt < 32; attempt++) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 2.0D * range;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 2.0D * range;
            int y = Mth.floor(player.getY()) + player.getRandom().nextInt(range * 2 + 1) - range;
            y = Mth.clamp(y, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
            BlockPos origin = BlockPos.containing(x, y, z);
            for (int offset = 0; offset <= 4; offset++) {
                BlockPos feet = origin.above(offset);
                if (!isSafeSpot(level, feet)) {
                    continue;
                }

                player.teleportTo(
                        level, feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D, player.getYRot(), player.getXRot());
                return true;
            }
        }
        return false;
    }

    private static boolean isSafeSpot(ServerLevel level, BlockPos feet) {
        if (!level.isLoaded(feet)) {
            return false;
        }
        BlockState below = level.getBlockState(feet.below());
        if (!below.blocksMotion()) {
            return false;
        }
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above())
                        .getCollisionShape(level, feet.above())
                        .isEmpty();
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (wearer.getRandom().nextDouble() >= FRConfig.NEBULOUS_CORE_DODGE_CHANCE.get()) {
            return;
        }
        if (wearer instanceof ServerPlayer serverPlayer && tryDodgeTeleport(serverPlayer)) {
            event.setInvulnerabilityTicks(20);
            event.setCanceled(true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemArcanum1.lore").append(" " + getVisDiscount(stack) + "%"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemArcanum2.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum3.lore"));
        tooltip.add(Component.translatable(
                "item.ItemArcanum4.lore", Math.round(FRConfig.NEBULOUS_CORE_DODGE_CHANCE.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemArcanum5.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum7.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum8.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
    }
}
