package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ItemDarkSunRing extends FRCurioItem
        implements FRRechargable, WearerTickBehaviour, IncomingDamageBehaviour {

    private static final List<ResourceKey<DamageType>> NEGATIONS =
            List.of(DamageTypes.LAVA, DamageTypes.IN_FIRE, DamageTypes.ON_FIRE);

    private static final double AMPLIFY_CHANCE = 0.25D;

    public ItemDarkSunRing(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.DARK_SUN_RING_MAX_CHARGE.get()
                : FRConfig.DARK_SUN_RING_MAX_CHARGE.getDefault();
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (wearer.isOnFire()) {
            wearer.clearFire();
        }
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        DamageSource source = event.getSource();

        if (NEGATIONS.stream().anyMatch(source::is)) {
            if (FRConfig.DARK_SUN_RING_HEAL_LIMIT.get()) {
                if (wearer.invulnerableTime == 0) {
                    wearer.heal(event.getAmount());
                    event.setInvulnerabilityTicks(20);
                }
            } else {
                wearer.heal(event.getAmount());
            }
            event.setCanceled(true);
            return;
        }

        if (source.getEntity() != null
                && !FRDamageTypes.isAbsolute(source)
                && wearer.invulnerableTime == 0
                && wearer.getRandom().nextDouble() <= FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get()) {
            wearer.invulnerableTime = 20;
            source.getEntity().hurt(source, event.getAmount());
            event.setCanceled(true);
            return;
        }

        if (event.getAmount() > FRConfig.DARK_SUN_RING_DAMAGE_CAP.get() && !FRDamageTypes.isAbsolute(source)) {
            wearer.displayClientMessage(Component.translatable("notification.overdamage_block"), true);
            event.setCanceled(true);
            return;
        }

        if (!FRDamageTypes.isAbsolute(source) && wearer.getRandom().nextDouble() <= AMPLIFY_CHANCE) {
            event.setAmount(event.getAmount()
                    + event.getAmount() * (float) wearer.getRandom().nextDouble());
        }
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDarkSunRing1.lore"));
        tooltip.add(Component.translatable(
                "item.ItemDarkSunRing2_1.lore", Math.round(FRConfig.DARK_SUN_RING_DAMAGE_CAP.get())));
        tooltip.add(Component.translatable("item.ItemDarkSunRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable(
                "item.ItemDarkSunRing4.lore", Math.round(FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get() * 100.0D)));
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
