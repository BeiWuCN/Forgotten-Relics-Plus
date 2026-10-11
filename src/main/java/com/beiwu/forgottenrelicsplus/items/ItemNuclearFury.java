package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityRageousMissile;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import vazkii.botania.common.handler.BotaniaSounds;

public class ItemNuclearFury extends FRItem implements FRRechargable, IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final int SHOT_INTERVAL = 2;

    private static final int VIS_INTERVAL = 20;

    private static final double HORIZONTAL_SPREAD = 3.1D;

    private static final double VERTICAL_OFFSET = 3.8D;

    private static final double VERTICAL_SPREAD = 1.55D;

    public ItemNuclearFury(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCostPerSecond() {
        return (int) (FRConfig.NUCLEAR_FURY_VIS_COST_PER_SECOND.get() * FRConfig.NUCLEAR_FURY_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.NUCLEAR_FURY_MAX_CHARGE.get()
                : FRConfig.NUCLEAR_FURY_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.NUCLEAR_FURY_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }

        if (remainingUseDuration <= 0 || remainingUseDuration == USE_DURATION) {
            return;
        }
        int elapsed = USE_DURATION - remainingUseDuration;

        if (elapsed > 0
                && elapsed % VIS_INTERVAL == SHOT_INTERVAL
                && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            player.stopUsingItem();
            return;
        }

        if (remainingUseDuration % SHOT_INTERVAL == 0) {
            fire(level, player);
        }
    }

    private static void fire(Level level, Player player) {
        double x = player.getX() + (level.random.nextDouble() - 0.5D) * HORIZONTAL_SPREAD;

        double y = player.getY() + VERTICAL_OFFSET + (level.random.nextDouble() - VERTICAL_SPREAD);
        double z = player.getZ() + (level.random.nextDouble() - 0.5D) * HORIZONTAL_SPREAD;

        EntityRageousMissile missile = new EntityRageousMissile(level, player, false);
        missile.setPos(x, y, z);
        if (level instanceof ServerLevel server) {

            SoundHelper.play(
                    level,
                    x,
                    y,
                    z,
                    BotaniaSounds.MISSILE,
                    SoundSource.PLAYERS,
                    0.6F,
                    0.8F + level.random.nextFloat() * 0.2F);

            server.sendParticles(ParticleTypes.WITCH, x, y, z, 8, 0.15D, 0.15D, 0.15D, 0.02D);

            FRParticles.serverSparkle(
                    server, player.getX(), player.getY() + 2.4D, player.getZ(), 1.0F, 0.4F, 1.0F, 6.0F, 6);
        }
        level.addFreshEntity(missile);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.NuclearFury1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.NuclearFury2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable(
                "item.NuclearFury3.lore",
                (int) FRConfig.NUCLEAR_FURY_DAMAGE_MIN.get().doubleValue(),
                (int) FRConfig.NUCLEAR_FURY_DAMAGE_MAX.get().doubleValue()));
    }
}
