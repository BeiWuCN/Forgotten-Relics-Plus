package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityChaoticOrb;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
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

public class ItemChaosTome extends FRItem implements FRRechargable, IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final double SEEKER_CHANCE = 0.35D;

    public ItemChaosTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_PRIMAL_CHAOS_VIS_COST.get() * FRConfig.TOME_OF_PRIMAL_CHAOS_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.TOME_OF_PRIMAL_CHAOS_MAX_CHARGE.get()
                : FRConfig.TOME_OF_PRIMAL_CHAOS_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_PRIMAL_CHAOS_WARP.get();
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
        int interval = FRConfig.TOME_OF_PRIMAL_CHAOS_COOLDOWN.get();

        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnOrb(level, player);
    }

    private static void spawnOrb(Level level, Player player) {

        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        boolean seeker = level.random.nextDouble() <= SEEKER_CHANCE;
        EntityChaoticOrb orb = new EntityChaoticOrb(level, player, seeker);
        double x = center.x + (level.random.nextDouble() - 0.5D) * 3.0D;
        double y = center.y + (level.random.nextDouble() - 0.5D) * 1.0D;
        double z = center.z + (level.random.nextDouble() - 0.5D) * 3.0D;
        orb.setPos(x, y, z);

        orb.setDeltaMovement(new Vec3(x, y, z).subtract(center).scale(0.2D + level.random.nextDouble() * 0.2D));

        SoundHelper.play(
                level,
                orb.getX(),
                orb.getY(),
                orb.getZ(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.3F,
                0.8F + level.random.nextFloat() * 0.1F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemChaosTome1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome2.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable("item.ItemChaosTome5.lore", (int)
                FRConfig.CHAOS_TOME_DAMAGE_CAP.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemChaosTome6.lore"));
    }
}
