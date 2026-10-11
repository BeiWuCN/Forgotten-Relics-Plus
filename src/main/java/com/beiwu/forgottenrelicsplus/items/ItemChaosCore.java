package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ItemChaosCore extends FRItem implements IWarpingGear, CarriedDamageBehaviour {

    private static final List<Holder<MobEffect>> EFFECT_POOL = List.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SPEED,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.DAMAGE_BOOST,
            MobEffects.WITHER,
            MobEffects.WITHER,
            MobEffects.JUMP,
            MobEffects.CONFUSION,
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.INVISIBILITY,
            MobEffects.BLINDNESS,
            MobEffects.NIGHT_VISION,
            MobEffects.HUNGER,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.HEALTH_BOOST);

    public ItemChaosCore(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {

        if (level.isClientSide() || !(entity instanceof LivingEntity wearer)) {
            return;
        }
        if (wearer.getRandom().nextDouble() > FRConfig.CHAOS_CORE_CHANCE.get()) {
            return;
        }
        Holder<MobEffect> effect = EFFECT_POOL.get(wearer.getRandom().nextInt(EFFECT_POOL.size()));
        int duration = FRConfig.CHAOS_CORE_DURATION_MIN.get()
                + wearer.getRandom().nextInt(FRConfig.CHAOS_CORE_DURATION_SPAN.get());
        int amplifier = wearer.getRandom().nextInt(FRConfig.CHAOS_CORE_MAX_AMPLIFIER.get() + 1);

        wearer.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true));
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (attacker.getRandom().nextDouble() >= FRConfig.CHAOS_CORE_REDIRECT_ATTACK.get()) {
            return;
        }
        LivingEntity random = randomNearby(event.getEntity(), attacker);
        if (random == null) {
            return;
        }
        float redirected = rollRedirected(event.getAmount(), attacker);
        if (attacker.getRandom().nextDouble() < FRConfig.CHAOS_CORE_SELF_REFLECT.get()) {
            attacker.hurt(event.getSource(), redirected);
        } else {
            random.hurt(event.getSource(), redirected);
        }
        event.setCanceled(true);
    }

    @Override
    public void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (victim.getRandom().nextDouble() >= FRConfig.CHAOS_CORE_REDIRECT_DEFEND.get()) {
            return;
        }
        LivingEntity random = randomNearby(victim, victim);
        if (random == null) {
            return;
        }
        random.hurt(event.getSource(), rollRedirected(event.getAmount(), victim));
        event.setCanceled(true);
    }

    @Override
    public void onCarriedHurt(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        event.setAmount(event.getAmount()
                * (float) (victim.getRandom().nextDouble() * FRConfig.CHAOS_CORE_DAMAGE_MULT_MAX.get()));
    }

    private static float rollRedirected(float amount, LivingEntity roller) {
        return (float) (amount * roller.getRandom().nextDouble() * FRConfig.CHAOS_CORE_DAMAGE_MULT_MAX.get());
    }

    private static LivingEntity randomNearby(LivingEntity center, LivingEntity roller) {
        List<LivingEntity> candidates = center.level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        center.getBoundingBox().inflate(16.0D),
                        entity -> entity != center && entity.isAlive());
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(roller.getRandom().nextInt(candidates.size()));
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.CHAOS_CORE_WARP.get();
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemChaosCore1.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore2.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore4.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore5.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore7.lore"));
    }
}
