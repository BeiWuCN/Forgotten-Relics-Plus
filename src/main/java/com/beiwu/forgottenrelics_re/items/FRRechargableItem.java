package com.beiwu.forgottenrelics_re.items;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 「可充能法术物品」基类。
 *
 * <p>1.12.2 原版里，符文天象石与空间魔镜都实现 {@code thaumcraft.api.items.IRechargable}，
 * 由 Thaumcraft 的 {@code RechargeHelper} 按玩家背包里的 Vis 给它们充能，
 * 施法时用 {@code RechargeHelper.consumeCharge(stack, player, cost)} 扣费。
 *
 * <p>1.21.1 的 Thaumaturge 保留了同一套 {@link IRechargable} 接口与
 * {@code api.items.RechargeAccess} 门面，只是充能来源从「玩家背包」换成了「周围灵气」。
 * 充能动作由 Thaumaturge 自己根据 {@link #showInHud} 的返回值驱动，
 * 这里只需要声明储量上限。
 *
 * <p>与饰品一样，本类同时提供统一的 Shift 展开式 tooltip。
 */
public abstract class FRRechargableItem extends Item implements IRechargable {

    public FRRechargableItem(Properties properties) {
        // 原版这两个物品都是 maxStackSize = 1。
        super(properties.stacksTo(1));
    }

    @Override
    public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        // 原版返回 EnumChargeDisplay.NORMAL，即始终在 HUD 上显示储量。
        return ChargeDisplay.NORMAL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            appendShiftTooltip(stack, context, tooltip, flag);
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    /** 子类在这里追加「按住 Shift 才显示」的详细说明。 */
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
