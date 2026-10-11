package com.beiwu.forgottenrelicsplus.items;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FRItem extends Item {

    public FRItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendShift(tooltip, () -> appendShiftTooltip(stack, context, tooltip, flag));
    }

    public static void appendShift(List<Component> tooltip, Runnable expanded) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            expanded.run();
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
