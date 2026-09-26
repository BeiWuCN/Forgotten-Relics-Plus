package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 远古之庇护（Ancient Aegis），1.12.2 原版 {@code ItemAncientAegis}，腰带槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>提供 {@code ancientAegisKnockbackResistance}（默认 1.0）的击退抗性；</li>
 *   <li>佩戴者每 {@code ancientAegisHealInterval}（默认 20）tick 回 {@code ancientAegisHealAmount}
 *       （默认 1 点）血，前提是没满血；</li>
 *   <li>佩戴者受到的伤害乘以 {@code 1 - ancientAegisDamageReduction}（默认减免 25%）；</li>
 *   <li>若伤害的承受者<b>没有</b>佩戴庇护，则把 40% 伤害转嫁给 32 格内一位佩戴者。</li>
 * </ul>
 *
 * <p>后两条要在伤害事件里做，见 {@code FRDamageEvents}；本类只负责属性修饰符、回血与 tooltip。
 * 属性修饰符交给 Curios 的 {@code getAttributeModifiers} 统一施加与撤销，不必像原版那样手动
 * 在装备/卸下时往属性表上加加减减。
 */
public class ItemAncientAegis extends FRCurioItem {

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
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity wearer) || wearer.level().isClientSide()) {
            return;
        }
        // 原版是在 onWornTick 里按 ticksExisted 取模判定，这里保持一致。
        int interval = FRConfig.ANCIENT_AEGIS_HEAL_INTERVAL.get();
        if (interval <= 0 || wearer.tickCount % interval != 0) {
            return;
        }
        if (wearer.getHealth() < wearer.getMaxHealth()) {
            wearer.heal(FRConfig.ANCIENT_AEGIS_HEAL_AMOUNT.get().floatValue());
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 原版把减免比例换算成百分比整数显示。
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
