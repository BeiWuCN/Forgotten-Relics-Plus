package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.utils.HandheldItemState;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FRItem extends Item {

    public FRItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!(this instanceof FRRechargable)) {
            return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
        }
        if (slotChanged || !ItemStack.isSameItem(oldStack, newStack) || oldStack.getCount() != newStack.getCount()) {
            return true;
        }
        return !HandheldItemState.sameVisibleComponents(oldStack.getComponents(), newStack.getComponents(),
                FRItem::isVisualComponent);
    }

    private static boolean isVisualComponent(DataComponentType<?> type) {
        return type != TTDataComponents.CHARGE.get() && type != TTDataComponents.RECHARGEABLE.get()
                && type != FRDataComponents.FATE_COOLDOWN.get() && type != FRDataComponents.DEVOURER_TARGET.get()
                && type != FRDataComponents.EDICT_TARGET.get() && type != FRDataComponents.VOID_GRIMOIRE_TARGET.get();
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
