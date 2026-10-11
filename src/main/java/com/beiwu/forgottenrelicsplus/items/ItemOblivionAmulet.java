package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ItemOblivionAmulet extends FRCurioItem
        implements FRRechargable, IWarpingGear, WearerTickBehaviour, IncomingDamageBehaviour {

    private static final List<Holder<MobEffect>> RANDOM_DEBUFFS =
            List.of(MobEffects.DIG_SLOWDOWN, MobEffects.BLINDNESS, MobEffects.WEAKNESS, MobEffects.WITHER);

    public ItemOblivionAmulet(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.OBLIVION_AMULET_MAX_CHARGE.get()
                : FRConfig.OBLIVION_AMULET_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.OBLIVION_AMULET_WARP.get();
    }

    public static float getStoredDamage(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.STORED_DAMAGE.get(), 0.0F);
    }

    public static void setStoredDamage(ItemStack stack, float value) {
        stack.set(FRDataComponents.STORED_DAMAGE.get(), Math.max(0.0F, value));
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        float stored = getStoredDamage(stack);

        if (stored > 0.0F && wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE.get()) {
            float released = (float) (stored * wearer.getRandom().nextDouble());
            if (released > FRConfig.OBLIVION_AMULET_DAMAGE_CAP.get()
                    && wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE.get()) {
                released = (float) (FRConfig.OBLIVION_AMULET_DAMAGE_CAP.get()
                        * wearer.getRandom().nextDouble());
            }
            setStoredDamage(stack, stored - released);
            wearer.hurt(FRDamageTypes.source(wearer.level(), FRDamageTypes.OBLIVION), released);
            return;
        }

        if (wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_POTION_CHANCE.get()) {
            Holder<MobEffect> effect = RANDOM_DEBUFFS.get(wearer.getRandom().nextInt(RANDOM_DEBUFFS.size()));
            int min = FRConfig.OBLIVION_AMULET_POTION_DURATION_MIN.get();
            int max = FRConfig.OBLIVION_AMULET_POTION_DURATION_MAX.get();
            int duration = min + wearer.getRandom().nextInt(Math.max(1, max - min));
            int levelMin = FRConfig.OBLIVION_AMULET_POTION_LEVEL_MIN.get();
            int levelMax = FRConfig.OBLIVION_AMULET_POTION_LEVEL_MAX.get();
            int amplifier = levelMin + wearer.getRandom().nextInt(Math.max(1, levelMax - levelMin));

            wearer.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
        }
    }

    @Override
    public int priority() {
        return 70;
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        int cost = (int) (event.getAmount() * 8.0F * FRConfig.OBLIVION_AMULET_VIS_MULT.get());
        if (!RechargeAccess.consumeCharge(stack, wearer, cost)) {
            return;
        }
        setStoredDamage(stack, getStoredDamage(stack) + event.getAmount());
        event.setCanceled(true);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemOblivionAmulet1.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet2.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        float stored = getStoredDamage(stack);
        if (stored > 0.0F) {
            tooltip.add(Component.translatable("item.ItemOblivionAmuletDamage.lore")
                    .append(" " + Math.round(stored * 100.0D) / 100.0D));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        }
    }
}
