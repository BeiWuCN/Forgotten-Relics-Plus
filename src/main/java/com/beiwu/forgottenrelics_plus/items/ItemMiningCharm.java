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
 * 采矿护符（Mining Charm）。
 *
 * <p>RE 行为（见 {@code ItemMiningCharm} 与 {@code RelicsEventHandler.miningStuff}）：
 * <ul>
 *   <li>占用 Baubles 的 RING 槽（RE 的 {@code getBaubleType} 返回 {@code BaubleType.RING}）；</li>
 *   <li>装备时给玩家加一条 {@code REACH_DISTANCE} 属性修饰符，数值取自配置
 *       {@code miningCharmReach}（默认 2）；</li>
 *   <li>挖掘速度不是属性，而是在 {@code PlayerEvent.BreakSpeed} 里按倍率乘上去，
 *       倍率取自配置 {@code miningCharmBoost}（默认 1.0，即 +100%）。</li>
 * </ul>
 *
 * <p>1.21.1 对应关系：
 * <ul>
 *   <li>Baubles 的 RING 槽 → Curios 的 {@code ring} 槽（由数据包 {@code data/curios/tags/item/ring.json} 指定）；</li>
 *   <li>{@code REACH_DISTANCE} → {@code Attributes.BLOCK_INTERACTION_RANGE}；</li>
 *   <li>{@code PlayerEvent.BreakSpeed} 在 NeoForge 中同名保留，见 {@code FRCommonEvents#onBreakSpeed}。</li>
 * </ul>
 */
public class ItemMiningCharm extends FRCurioItem implements BreakSpeedBehaviour {

    public ItemMiningCharm(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
        modifiers.put(Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(id, FRConfig.MINING_CHARM_REACH.get(), AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public float breakSpeedBoost(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        return FRConfig.MINING_CHARM_BOOST.get().floatValue();
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 与 RE 一致：显示的是「百分比整数」，1.0 显示成 100。
        tooltip.add(Component.translatable("item.ItemMiningCharm1.lore", Math.round(FRConfig.MINING_CHARM_BOOST.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemMiningCharm2.lore", FRConfig.MINING_CHARM_REACH.get()));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
