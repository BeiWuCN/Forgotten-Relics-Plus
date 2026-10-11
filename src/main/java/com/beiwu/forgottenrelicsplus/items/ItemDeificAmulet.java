package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
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

public class ItemDeificAmulet extends FRCurioItem
        implements FRRechargable, WearerTickBehaviour, IncomingDamageBehaviour {

    public ItemDeificAmulet(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.DEIFIC_AMULET_MAX_CHARGE.get()
                : FRConfig.DEIFIC_AMULET_MAX_CHARGE.getDefault();
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

            int cost = (int) (FRConfig.DEIFIC_AMULET_VIS_COST.get() * FRConfig.DEIFIC_AMULET_VIS_MULT.get());
            if (RechargeAccess.consumeCharge(stack, wearer, cost)) {
                wearer.setAirSupply(FRConfig.DEIFIC_AMULET_AIR_SUPPLY.get());
            }
        }

        tickInvincibility(stack, wearer);
    }

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

    private void tickInvincibility(ItemStack stack, LivingEntity wearer) {
        int cooldown = stack.getOrDefault(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), 0);
        if (cooldown > 0) {
            stack.set(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), cooldown - 1);
        }
    }

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
        stack.set(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), FRConfig.DEIFIC_AMULET_INVINCIBILITY_COOLDOWN.get());
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        if (FRConfig.DEIFIC_AMULET_EFFECT_IMMUNITY.get()) {
            tooltip.add(Component.translatable(
                    FRConfig.DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS.get()
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
