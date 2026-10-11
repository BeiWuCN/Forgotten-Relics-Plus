package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class ItemFateTome extends FRItem implements FRRechargable, IWarpingGear, DeathPreventionBehaviour {

    private static final int PRIORITY = 200;

    private static final int BURST_COLOR = 0x9FE8FF;

    public ItemFateTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_BROKEN_FATES_VIS_COST.get() * FRConfig.TOME_OF_BROKEN_FATES_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.TOME_OF_BROKEN_FATES_MAX_CHARGE.get()
                : FRConfig.TOME_OF_BROKEN_FATES_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_BROKEN_FATES_WARP.get();
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        int cooldown = stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0);
        if (cooldown > 0) {
            int remaining = cooldown - 1;
            stack.set(FRDataComponents.FATE_COOLDOWN.get(), remaining);
            if (remaining == 0) {
                player.displayClientMessage(Component.translatable("notification.fate_cooldown_over"), true);
            }
        }

        if (player.getRandom().nextDouble() <= FRConfig.TOME_OF_BROKEN_FATES_MULTI_HELD_CHANCE.get()
                && countInMainInventory(player) > 1) {
            insanelyDisastrousConsequences(player);
        }
    }

    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {

        if (stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0) > 0) {
            return;
        }

        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }

        int maxSeconds = FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get();
        if (maxSeconds != 0) {
            int minTicks = FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MIN.get() * 20;
            int bonusTicks = maxSeconds * 20 - minTicks;

            int ticks = bonusTicks > 0 ? (int) (minTicks + player.getRandom().nextDouble() * bonusTicks) : minTicks;
            stack.set(FRDataComponents.FATE_COOLDOWN.get(), ticks);
        }
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        applyFateEffects(player);
        playFateFeedback(player);
    }

    private static void applyFateEffects(Player player) {
        if (player.getRandom().nextDouble() <= FRConfig.TOME_OF_BROKEN_FATES_BUFF_CHANCE.get()) {

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0));
        } else {

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1));
        }
    }

    private static void playFateFeedback(Player player) {
        if (player.level() instanceof ServerLevel server) {
            double y = player.getY() + 1.0D;

            FRParticles.serverWispBurst(
                    server,
                    player.getX(),
                    y,
                    player.getZ(),
                    0.0F,
                    (float) (0.8D + server.random.nextDouble() * 0.2D),
                    (float) (0.4D + server.random.nextDouble() * 0.6D),
                    1.25F,
                    1.0F,
                    1,
                    0.0D,
                    0.0D);
            server.sendParticles(
                    ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, BURST_COLOR),
                    player.getX(),
                    y,
                    player.getZ(),
                    32,
                    0.4D,
                    0.4D,
                    0.4D,
                    0.1D);
        }

        SoundHelper.play(
                player.level(),
                player.getX() + 0.5D,
                player.getY() + 1.5D,
                player.getZ() + 0.5D,
                SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
    }

    private static void insanelyDisastrousConsequences(Player player) {
        Level level = player.level();
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() == FRItems.TOME_OF_BROKEN_FATES.get()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        double range = FRConfig.TOME_OF_BROKEN_FATES_MULTI_HELD_RANGE.get();
        AABB area = new AABB(
                player.getX() - range,
                player.getY() - range,
                player.getZ() - range,
                player.getX() + range,
                player.getY() + range,
                player.getZ() + range);
        float damage = FRConfig.TOME_OF_BROKEN_FATES_DAMAGE.get().floatValue();
        float radius = FRConfig.TOME_OF_BROKEN_FATES_EXPLOSION_RADIUS.get().floatValue();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            target.hurt(FRDamageTypes.source(level, FRDamageTypes.FATE), damage);
            level.explode(
                    player,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    radius,
                    false,
                    Level.ExplosionInteraction.NONE);
        }

        level.explode(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                FRConfig.TOME_OF_BROKEN_FATES_BIG_EXPLOSION_RADIUS.get().floatValue(),
                false,
                Level.ExplosionInteraction.NONE);
    }

    private static int countInMainInventory(Player player) {
        Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() == FRItems.TOME_OF_BROKEN_FATES.get()) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.FateTome1.lore"));
        tooltip.add(Component.translatable("item.FateTome2.lore"));
        tooltip.add(Component.translatable("item.FateTome3.lore"));
        tooltip.add(Component.translatable("item.FateTome4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        if (FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get() != 0) {
            tooltip.add(Component.translatable(
                    "item.FateTome5.lore",
                    FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MIN.get(),
                    FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get()));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        }
        tooltip.add(Component.translatable("item.FateTome6.lore"));
        tooltip.add(Component.translatable("item.FateTome7.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FateTome8.lore"));
        tooltip.add(Component.translatable("item.FateTome9.lore"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int cooldown = stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0);
        if (cooldown <= 0) {
            return;
        }
        double seconds = BigDecimal.valueOf(cooldown / 20.0D)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable(
                        "item.FRCode" + ThreadLocalRandom.current().nextInt(1, 16) + ".lore")
                .append(Component.translatable("item.FateTomeCooldown.lore"))
                .append(" " + seconds + " ")
                .append(Component.translatable("item.FRSeconds.lore")));
    }
}
