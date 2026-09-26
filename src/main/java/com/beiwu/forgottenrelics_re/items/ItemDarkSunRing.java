package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 七阳之戒（Ring of The Seven Suns），1.12.2 原版 {@code ItemDarkSunRing}，戒指槽。
 *
 * <p>原版行为（伤害相关的部分在 {@code FRDamageEvents} 里）：
 * <ul>
 *   <li>佩戴者着火时立刻熄灭；</li>
 *   <li>完全抵消任何超过 {@code darkSunRingDamageCap}（默认 100）点的单次伤害，并弹一条提示；</li>
 *   <li>火焰与岩浆伤害不生效，反而按伤害量回血（可用 {@code darkSunRingHealLimit} 加冷却）；</li>
 *   <li>有 {@code darkSunRingDeflectChance}（默认 20%）的概率把攻击反弹给攻击者；</li>
 *   <li>另有 25% 概率让受到的普通攻击变得更强——这是原版刻意设计的诅咒面。</li>
 * </ul>
 *
 * <p>它同时是 Thaumaturge 的「可充能」物品，储量上限来自 {@code darkSunRingMaxCharge}（默认 500）。
 */
public class ItemDarkSunRing extends FRRechargableCurioItem {

    public ItemDarkSunRing(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DARK_SUN_RING_MAX_CHARGE.get();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof LivingEntity wearer && wearer.isOnFire()) {
            wearer.clearFire();
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDarkSunRing1.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing2_1.lore",
                Math.round(FRConfig.DARK_SUN_RING_DAMAGE_CAP.get())));
        tooltip.add(Component.translatable("item.ItemDarkSunRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing4.lore",
                Math.round(FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemDarkSunRing5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing6.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing7.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing8.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
