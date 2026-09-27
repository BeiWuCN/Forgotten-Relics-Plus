package com.beiwu.forgottenrelics_re.items;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 物品基类：统一提供「Shift 展开式 tooltip」。
 *
 * <p>原版把这段逻辑在 {@code ItemModBase}、{@code ItemBaubleBase} 里各写了一遍；这里提到唯一的
 * 基类上，子类只需要覆写 {@link #appendShiftTooltip} 填内容，不用再关心按键判断与首尾空行。
 *
 * <p>语言键沿用原版的 {@code item.FRShiftTooltip.lore} 与 {@code item.FREmpty.lore}。
 */
public class FRItem extends Item {

    public FRItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendShift(tooltip, () -> appendShiftTooltip(stack, context, tooltip, flag));
    }

    /**
     * Shift 展开逻辑本身。
     *
     * <p>做成静态方法是为了让「必须继承原版物品类」的物品也能复用：恐惧之冠继承了
     * {@link net.minecraft.world.item.ArmorItem}，Java 单继承下没法再继承本类。
     */
    public static void appendShift(List<Component> tooltip, Runnable expanded) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            expanded.run();
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    /**
     * 子类在这里追加「按住 Shift 才显示」的详细说明。
     *
     * <p>原版每条文案自带 {@code §5} 之类的颜色代码，这里沿用同一套语言键，无需额外设置样式。
     */
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
