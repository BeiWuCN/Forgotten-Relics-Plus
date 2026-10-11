package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityLunarFlare;
import com.beiwu.forgottenrelicsplus.registry.FRSounds;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemLunarFlares extends FRItem implements FRRechargable, IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final double LOOK_RANGE = 128.0D;

    private static final double SPREAD = 18.0D;

    private static final double HEIGHT_BASE = 24.0D;

    private static final double LAUNCH_SPEED = 4.0D;

    public ItemLunarFlares(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_LUNAR_FLARES_VIS_COST.get() * FRConfig.TOME_OF_LUNAR_FLARES_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.TOME_OF_LUNAR_FLARES_MAX_CHARGE.get()
                : FRConfig.TOME_OF_LUNAR_FLARES_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_LUNAR_FLARES_WARP.get();
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
        int interval = FRConfig.TOME_OF_LUNAR_FLARES_COOLDOWN.get();

        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }
        BlockHitResult hit = findPointedBlock(level, player);
        if (hit == null) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnLunarFlare(level, player, hit);

        if (remainingUseDuration % 4 == 0) {
            SoundHelper.play(
                    level,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    FRSounds.STARFALL.get(),
                    SoundSource.PLAYERS,
                    2.0F,
                    1.0F + level.random.nextFloat() * 0.5F);
        }
    }

    private static BlockHitResult findPointedBlock(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_RANGE));
        BlockHitResult hit =
                level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    private static void spawnLunarFlare(Level level, Player player, BlockHitResult hit) {
        int x = hit.getBlockPos().getX();
        int y = hit.getBlockPos().getY();
        int z = hit.getBlockPos().getZ();

        EntityLunarFlare flare = new EntityLunarFlare(level, player, x, y, z);
        double offsetX = (level.random.nextDouble() - 0.5D) * SPREAD;
        double offsetY = (HEIGHT_BASE + (level.random.nextDouble() - 0.5D) * SPREAD) * 2.0D;
        double offsetZ = (level.random.nextDouble() - 0.5D) * SPREAD;
        flare.setPos(x + 0.5D + offsetX, y + offsetY, z + 0.5D + offsetZ);
        flare.setDeltaMovement(new Vec3(offsetX, offsetY, offsetZ).normalize().scale(-LAUNCH_SPEED));
        level.addFreshEntity(flare);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.LunarFlares1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.LunarFlares2.lore"));

        tooltip.add(Component.translatable("item.LunarFlares3.lore", (int)
                FRConfig.TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.LunarFlares35.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable("item.LunarFlares4.lore", (int)
                FRConfig.TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE.get().doubleValue()));
    }
}
