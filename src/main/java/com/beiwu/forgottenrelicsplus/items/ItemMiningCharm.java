package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.BreakSpeedBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
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

        tooltip.add(Component.translatable("item.ItemMiningCharm1.lore", Math.round(FRConfig.MINING_CHARM_BOOST.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemMiningCharm2.lore", FRConfig.MINING_CHARM_REACH.get()));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
