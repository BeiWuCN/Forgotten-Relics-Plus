package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.AllyProtectionBehaviour;
import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.CurioHelper;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
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

/**
 * 远古之庇护（Ancient Aegis），RE 的 {@code ItemAncientAegis}，腰带槽。
 *
 * <p>RE 行为：
 * <ul>
 *   <li>提供 {@code ancientAegisKnockbackResistance}（默认 1.0）的击退抗性；</li>
 *   <li>佩戴者每 {@code ancientAegisHealInterval}（默认 20）tick 回 {@code ancientAegisHealAmount}
 *       （默认 1 点）血，前提是没满血；</li>
 *   <li>佩戴者受到的伤害乘以 {@code 1 - ancientAegisDamageReduction}（默认减免 25%）；</li>
 *   <li>若伤害的承受者没有佩戴庇护，则把 40% 伤害转嫁给 32 格内一位佩戴者。</li>
 * </ul>
 *
 * <p>最后一条与其它物品不同：触发者是<b>别人</b>身上的庇护，而不是受害者自己身上的东西，
 * 所以它实现的是 {@link AllyProtectionBehaviour}，由派发器反向查找附近的佩戴者。
 * 属性修饰符交给 Curios 的 {@code getAttributeModifiers} 统一施加与撤销，不必像 RE 那样手动
 * 在装备/卸下时往属性表上加加减减。
 */
public class ItemAncientAegis extends FRCurioItem
        implements WearerTickBehaviour, IncomingDamageBehaviour, AllyProtectionBehaviour {

    /** 伤害转嫁半径：RE {@code findPlayerWithBauble} 里写死的 32 格。 */
    private static final double RELAY_RADIUS = 32.0D;

    /** 转嫁比例：RE 同样是写死的 40%，没有做成配置。 */
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
        // RE 是在 onWornTick 里按 ticksExisted 取模判定，这里保持一致。
        int interval = FRConfig.ANCIENT_AEGIS_HEAL_INTERVAL.get();
        if (interval <= 0 || wearer.tickCount % interval != 0) {
            return;
        }
        if (wearer.getHealth() < wearer.getMaxHealth()) {
            wearer.heal(FRConfig.ANCIENT_AEGIS_HEAL_AMOUNT.get().floatValue());
        }
    }

    /** 佩戴者自己的减伤。转嫁是另一条路径，见 {@link #onAllyDamage}。 */
    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        float reduction = FRConfig.ANCIENT_AEGIS_DAMAGE_REDUCTION.get().floatValue();
        event.setAmount(event.getAmount() * (1.0F - reduction));
    }

    /**
     * 把没戴庇护的受害者的一部分伤害揽到自己身上。
     *
     * <p>受害者自己戴着庇护时不接管：那种情况已经在 {@link #onIncomingDamage} 里按配置减伤了，
     * RE 也是这么分流的（{@code if (isEquipped) { 减伤; return; }}）。
     */
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

    /** 在分摊与吸收之前（RE 源码 {@code :256} / {@code :260}）。 */
    @Override
    public int priority() {
        return 50;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // RE 把减免比例换算成百分比整数显示。
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
