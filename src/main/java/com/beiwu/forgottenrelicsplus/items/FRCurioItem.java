package com.beiwu.forgottenrelicsplus.items;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public abstract class FRCurioItem extends FRItem implements ICurioItem {

    protected FRCurioItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = com.google.common.collect.LinkedHashMultimap.create();

        if (slotContext.entity() != null) {
            fillAttributeModifiers(modifiers, slotContext, stack, id);
        }
        return modifiers;
    }

    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {

        return true;
    }
}
