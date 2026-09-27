package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 神圣护身符（Deific Amulet），RE 的 {@code ItemDeificAmulet}，护身符槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>{@code deificAmuletEffectImmunity} 开启时持续清除佩戴者身上的状态效果；
 *       若同时开启 {@code deificAmuletOnlyNegatesDebuffs}，则只清减益、保留增益；</li>
 *   <li>着火时立刻熄灭；</li>
 *   <li>氧气耗尽时扣 Vis，把氧气补回来；</li>
 *   <li>{@code deificAmuletInvincibility} 开启时延长无敌帧，并有独立冷却。</li>
 * </ul>
 *
 * <p>注意一处刻意的差异：RE 在窒息分支里调用的是 {@code entity.setFire(...)}，
 * 但同一段代码上面刚写过「着火就熄灭」，语言键写的是「在水下自动补充氧气」，
 * 配置注释也写的是「prevents suffocation」。据此判断原版是笔误，这里按<b>补氧气</b>实现，
 * 配置项改用 {@code deificAmuletAirSupply}（默认 300 tick，即一管氧气）。
 */
public class ItemDeificAmulet extends FRCurioItem
        implements FRRechargable, WearerTickBehaviour, IncomingDamageBehaviour {

    public ItemDeificAmulet(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DEIFIC_AMULET_MAX_CHARGE.get();
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (FRConfig.DEIFIC_AMULET_EFFECT_IMMUNITY.get()) {
            clearEffects(wearer);
        }

        if (wearer.isOnFire()) {
            wearer.clearFire();
        }

        if (wearer.getAirSupply() <= 0) {
            // 原版这里是 setFire(fireDuration)，按语言键与配置说明判断应为「补充氧气」，故改为此写法。
            int cost = (int) (FRConfig.DEIFIC_AMULET_VIS_COST.get() * FRConfig.DEIFIC_AMULET_VIS_MULT.get());
            if (RechargeAccess.consumeCharge(stack, wearer, cost)) {
                wearer.setAirSupply(FRConfig.DEIFIC_AMULET_AIR_SUPPLY.get());
            }
        }

        tickInvincibility(stack, wearer);
    }

    /**
     * 清除状态效果。
     *
     * <p>原版区分「只清减益」与「全清」两种模式。1.21.1 判断一个效果是不是减益用
     * {@code MobEffect#getCategory() == MobEffectCategory.HARMFUL}。清除时先拷一份列表，
     * 避免遍历过程中改动 {@code getActiveEffects()} 抛并发修改异常。
     */
    private static void clearEffects(LivingEntity wearer) {
        if (FRConfig.DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS.get()) {
            List<Holder<MobEffect>> harmful = new ArrayList<>();
            for (MobEffectInstance instance : wearer.getActiveEffects()) {
                if (!instance.getEffect().value().isBeneficial()) {
                    harmful.add(instance.getEffect());
                }
            }
            harmful.forEach(wearer::removeEffect);
        } else {
            wearer.removeAllEffects();
        }
    }

    /**
     * 延长无敌帧。
     *
     * <p>原版在 {@code onWornTick} 里读 {@code entity.hurtResistantTime}：刚被打过
     * （无敌时间大于 10 tick）时把无敌帧拉长到 {@code deificAmuletInvincibilityExtension}，
     * 并写下 {@code deificAmuletInvincibilityCooldown} 的冷却，冷却期间不再触发。
     * 1.21.1 里这个字段叫 {@code invulnerableTime} 且是只读的，所以改用
     * {@code LivingIncomingDamageEvent#setInvulnerabilityTicks}，见 {@link #onIncomingDamage}；
     * 这里只负责递减冷却。
     */
    private void tickInvincibility(ItemStack stack, LivingEntity wearer) {
        int cooldown = stack.getOrDefault(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), 0);
        if (cooldown > 0) {
            stack.set(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), cooldown - 1);
        }
    }

    /**
     * 排在最前。
     *
     * <p>原版把无敌帧延长写在 {@code ItemDeificAmulet.onWornTick} 里、按 tick 直接设置
     * {@code hurtResistantTime}，与其它物品的伤害处理<b>互相独立</b>，谁取消都不影响它。
     * 本移植的派发器在事件被取消时会停止后续派发，所以这里必须给它最小的优先级，
     * 才能保住「独立生效」这个语义。
     */
    @Override
    public int priority() {
        return 5;
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (!FRConfig.DEIFIC_AMULET_INVINCIBILITY.get()) {
            return;
        }
        if (stack.getOrDefault(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), 0) > 0) {
            return;
        }
        event.setInvulnerabilityTicks(FRConfig.DEIFIC_AMULET_INVINCIBILITY_EXTENSION.get());
        stack.set(FRDataComponents.INVINCIBILITY_COOLDOWN.get(),
                FRConfig.DEIFIC_AMULET_INVINCIBILITY_COOLDOWN.get());
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 原版：是否显示「免疫状态效果」这一行取决于配置；只清减益时用另一条文案。
        if (FRConfig.DEIFIC_AMULET_EFFECT_IMMUNITY.get()) {
            tooltip.add(Component.translatable(FRConfig.DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS.get()
                    ? "item.ItemDeificAmulet1_alt.lore"
                    : "item.ItemDeificAmulet1.lore"));
        }
        tooltip.add(Component.translatable("item.ItemDeificAmulet2.lore"));
        if (FRConfig.DEIFIC_AMULET_INVINCIBILITY.get()) {
            tooltip.add(Component.translatable("item.ItemDeificAmulet3.lore"));
        }
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDeificAmulet4.lore"));
        tooltip.add(Component.translatable("item.ItemDeificAmulet5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
    }
}
