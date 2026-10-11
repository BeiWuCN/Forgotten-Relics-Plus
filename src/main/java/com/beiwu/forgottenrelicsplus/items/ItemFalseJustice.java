package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ItemFalseJustice extends FRItem implements IWarpingGear, CarriedDamageBehaviour, DeathPreventionBehaviour {

    public ItemFalseJustice(Properties properties) {
        super(properties);
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        convertToTrueDamage(event, victim);
    }

    @Override
    public void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
        convertToTrueDamage(event, event.getEntity());
    }

    private static void convertToTrueDamage(LivingIncomingDamageEvent event, LivingEntity target) {
        DamageSource source = event.getSource();
        if (FRDamageTypes.isAbsolute(source)) {
            return;
        }
        float amount = event.getAmount()
                * FRConfig.FALSE_JUSTICE_DAMAGE_MULTIPLIER.get().floatValue();
        event.setCanceled(true);
        DamageSource converted = source.getEntity() == null
                ? FRDamageTypes.source(target.level(), FRDamageTypes.TRUE_DAMAGE_UNDEF)
                : FRDamageTypes.source(target.level(), FRDamageTypes.TRUE_DAMAGE, source.getEntity());
        target.hurt(converted, amount);
    }

    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {
        if (!FRConfig.FALSE_JUSTICE_PREVENT_DEATH.get() || isTrueDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        keepAlive(player);
    }

    @Override
    public void onLethalDamageCaused(LivingDeathEvent event, Player attacker, ItemStack stack) {
        if (!FRConfig.FALSE_JUSTICE_PREVENT_DEATH.get()
                || !(event.getEntity() instanceof LivingEntity victim)
                || isTrueDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        keepAlive(victim);
    }

    private static boolean isTrueDamage(DamageSource source) {
        return source.is(FRDamageTypes.TRUE_DAMAGE) || source.is(FRDamageTypes.TRUE_DAMAGE_UNDEF);
    }

    private static void keepAlive(LivingEntity entity) {
        entity.setHealth(1.0F);
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.FALSE_JUSTICE_WARP.get();
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.FalseJustice1.lore"));
        tooltip.add(Component.translatable("item.FalseJustice2.lore"));
        tooltip.add(Component.translatable("item.FalseJustice3.lore"));
    }
}
