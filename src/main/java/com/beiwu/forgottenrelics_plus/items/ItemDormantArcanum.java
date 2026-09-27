package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.CurioHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 休眠浑浊之核（Dormant Nebulous Core，注册名 {@code dormant_arcanum}），
 * 1.12.2 原版 {@code ItemDormantArcanum}，护符槽。
 *
 * <p>浑浊之核陷入休眠后的形态，所有效果失效。佩戴期间每 tick 消耗
 * {@code dormantArcanumVisCostPerTick}（默认 3）点 Vis 来递减剩余寿命；
 * 寿命归零就自动变回浑浊之核。寿命每次 tick 反而会「唤醒」得更快，
 * 与原版一致——它消耗的是物品自身的 Vis 储量。
 *
 * <p>它没有独立的研究词条：原版里只能由浑浊之核转化而来，配方也是没有的。
 */
public class ItemDormantArcanum extends FRCurioItem implements FRRechargable, WearerTickBehaviour {

    public ItemDormantArcanum(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DORMANT_ARCANUM_MAX_CHARGE.get();
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (!(wearer instanceof ServerPlayer player)) {
            return;
        }
        int lifetime = stack.getOrDefault(FRDataComponents.DORMANT_LIFETIME.get(), 0);
        if (lifetime > 0) {
            // 有 Vis 就扣一点、寿命减一；没 Vis 就维持休眠，等灵气慢慢补。
            if (RechargeAccess.consumeCharge(stack, player, FRConfig.DORMANT_ARCANUM_VIS_COST_PER_TICK.get())) {
                stack.set(FRDataComponents.DORMANT_LIFETIME.get(), lifetime - 1);
            }
            return;
        }
        // 寿命耗尽：原地变回浑浊之核。
        CurioHelper.replaceFirst(player, FRItems.DORMANT_ARCANUM.get(), new ItemStack(FRItems.ARCANUM.get()));
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDormantArcanum1.lore"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int lifetime = stack.getOrDefault(FRDataComponents.DORMANT_LIFETIME.get(), 0);
        if (lifetime > 0) {
            // 原版把寿命乘以 2 当作秒数显示，前面接一段「剩余时间」前缀、后面接单位。
            tooltip.add(Component.translatable("item.FRCode6.lore")
                    .append(String.valueOf(lifetime * 2))
                    .append(Component.translatable("item.ItemDormantArcanum2.lore")));
        }
    }
}
