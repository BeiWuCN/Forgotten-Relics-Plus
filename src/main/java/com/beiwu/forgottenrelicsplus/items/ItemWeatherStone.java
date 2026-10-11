package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemWeatherStone extends FRItem implements FRRechargable {

    public ItemWeatherStone(Properties properties) {
        super(properties);
    }

    public int getVisCost() {
        return (int) (FRConfig.WEATHER_STONE_VIS_COST.get() * FRConfig.WEATHER_STONE_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.WEATHER_STONE_MAX_CHARGE.get()
                : FRConfig.WEATHER_STONE_MAX_CHARGE.getDefault();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FRConfig.WEATHER_STONE_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isRaining() && !CooldownHelper.isOnCooldown(player)) {
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        Vec3 center = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);

        if (level instanceof ServerLevel serverLevel) {
            FRParticles.serverWispBurst(
                    serverLevel,
                    center.x,
                    center.y,
                    center.z,
                    0.0F,
                    0.3F + level.random.nextFloat() * 0.5F,
                    0.8F + level.random.nextFloat() * 0.2F,
                    0.2F + level.random.nextFloat() * 0.2F,
                    1.0F,
                    25,
                    0.0D,
                    0.043D);
        }

        if (remainingUseDuration != 1) {
            return;
        }
        if (!level.isRaining()) {
            return;
        }
        if (!com.leclowndu93150.thaumaturge.api.items.RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        SoundHelper.play(
                level, center.x, center.y, center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, (float)
                        (0.8D + Math.random() * 0.2D));
        if (level instanceof ServerLevel serverLevel) {

            serverLevel.setWeatherParameters(24000 + (int) (Math.random() * 976000.0D), 0, false, false);
        }
        CooldownHelper.setCooldown(player, FRConfig.WEATHER_STONE_COOLDOWN.get());
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemWeatherStone1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemWeatherStone2.lore", getVisCost()));
    }
}
