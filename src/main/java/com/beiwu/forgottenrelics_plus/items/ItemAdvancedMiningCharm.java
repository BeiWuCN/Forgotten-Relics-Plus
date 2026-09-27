package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.BreakSpeedBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 以太采矿护符（Ethereal Mining Charm）。
 *
 * <p>采矿护符的升级版。原版 {@code ItemAdvancedMiningCharm} 的行为与
 * {@link ItemMiningCharm} 完全一致，只是数值换成
 * {@code advancedMiningCharmBoost}（默认 3.0，即 +300%）与
 * {@code advancedMiningCharmReach}（默认 4）。
 *
 * <p>两个护符同时装备时，挖掘速度加成按原版 {@code RelicsEventHandler.miningStuff} 的写法
 * <b>相加</b>（{@code 1.0 + 3.0 + 1.0 = 5.0}，即 +400%），不是相乘。
 */
public class ItemAdvancedMiningCharm extends FRCurioItem implements BreakSpeedBehaviour {

    public ItemAdvancedMiningCharm(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
        modifiers.put(Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(id, FRConfig.ADVANCED_MINING_CHARM_REACH.get(), AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public float breakSpeedBoost(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        return FRConfig.ADVANCED_MINING_CHARM_BOOST.get().floatValue();
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemAdvancedMiningCharm1.lore", Math.round(FRConfig.ADVANCED_MINING_CHARM_BOOST.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemAdvancedMiningCharm2.lore", FRConfig.ADVANCED_MINING_CHARM_REACH.get()));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
