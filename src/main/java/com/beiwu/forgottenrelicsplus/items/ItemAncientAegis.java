package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.AllyProtectionBehaviour;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.CurioHelper;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class ItemAncientAegis extends FRCurioItem
        implements WearerTickBehaviour, IncomingDamageBehaviour, AllyProtectionBehaviour {

    private static final double RELAY_RADIUS = 32.0D;

    private static final float RELAY_FRACTION = 0.4F;

    public ItemAncientAegis(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
        modifiers.put(Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(id, FRConfig.ANCIENT_AEGIS_KNOCKBACK_RESISTANCE.get(),
                        AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {

        int interval = FRConfig.ANCIENT_AEGIS_HEAL_INTERVAL.get();
        if (interval <= 0 || wearer.tickCount % interval != 0) {
            return;
        }
        if (wearer.getHealth() < wearer.getMaxHealth()) {
            wearer.heal(FRConfig.ANCIENT_AEGIS_HEAL_AMOUNT.get().floatValue());
        }
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        float reduction = FRConfig.ANCIENT_AEGIS_DAMAGE_REDUCTION.get().floatValue();
        event.setAmount(event.getAmount() * (1.0F - reduction));
    }

    @Override
    public boolean onAllyDamage(LivingIncomingDamageEvent event, Player victim, Player guardian, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return false;
        }
        if (CurioHelper.isEquipped(victim, FRItems.ANCIENT_AEGIS.get())) {
            return false;
        }
        guardian.hurt(event.getSource(), event.getAmount() * RELAY_FRACTION);
        event.setAmount(event.getAmount() * (1.0F - RELAY_FRACTION));
        return true;
    }

    @Override
    public double radius() {
        return RELAY_RADIUS;
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemAncientAegis1.lore",
                Math.round(FRConfig.ANCIENT_AEGIS_DAMAGE_REDUCTION.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemAncientAegis2.lore"));
        tooltip.add(Component.translatable("item.ItemAncientAegis3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemAncientAegis4.lore"));
        tooltip.add(Component.translatable("item.ItemAncientAegis5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRBelt.lore"));
    }
}
