package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.CurioHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemDormantArcanum extends FRCurioItem implements FRRechargable, WearerTickBehaviour {

    public ItemDormantArcanum(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.DORMANT_ARCANUM_MAX_CHARGE.get()
                : FRConfig.DORMANT_ARCANUM_MAX_CHARGE.getDefault();
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (!(wearer instanceof ServerPlayer player)) {
            return;
        }
        int lifetime = stack.getOrDefault(FRDataComponents.DORMANT_LIFETIME.get(), 0);
        if (lifetime > 0) {

            if (RechargeAccess.consumeCharge(stack, player, FRConfig.DORMANT_ARCANUM_VIS_COST_PER_TICK.get())) {
                stack.set(FRDataComponents.DORMANT_LIFETIME.get(), lifetime - 1);
            }
            return;
        }
        CurioHelper.replaceFirst(player, FRItems.DORMANT_ARCANUM.get(), new ItemStack(FRItems.ARCANUM.get()));
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDormantArcanum1.lore"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int lifetime = stack.getOrDefault(FRDataComponents.DORMANT_LIFETIME.get(), 0);
        if (lifetime > 0) {

            tooltip.add(Component.translatable("item.FRCode6.lore")
                    .append(String.valueOf(lifetime * 2))
                    .append(Component.translatable("item.ItemDormantArcanum2.lore")));
        }
    }
}
