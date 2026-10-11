package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityBabylonWeapon;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

public class ItemApotheosis extends FRItem implements FRRechargable, IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final double AVOID_RADIUS = 2.0D;

    public ItemApotheosis(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.APOTHEOSIS_VIS_COST.get() * FRConfig.APOTHEOSIS_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.APOTHEOSIS_MAX_CHARGE.get()
                : FRConfig.APOTHEOSIS_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.APOTHEOSIS_WARP.get();
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
        int interval = FRConfig.APOTHEOSIS_COOLDOWN.get();

        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration == USE_DURATION
                || remainingUseDuration % interval != 0) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnBabylonWeapon(level, player);
    }

    private static void spawnBabylonWeapon(Level level, Player player) {
        Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);

        double rawYaw = player.getYHeadRot();
        double yaw = rawYaw < 0.0D ? Math.abs(rawYaw) : 360.0D - rawYaw;
        double x = 0.0D;
        double z = 0.0D;
        if (yaw >= 0.0D && yaw <= 90.0D) {
            double m = yaw / 90.0D;
            z = 1.0D - m;
            x = m;
        } else if (yaw <= 180.0D) {
            double m = (yaw - 90.0D) / 90.0D;
            x = 1.0D - m;
            z = -m;
        } else if (yaw <= 270.0D) {
            double m = (yaw - 180.0D) / 90.0D;
            z = -(1.0D - m);
            x = -m;
        } else {
            double m = (yaw - 270.0D) / 90.0D;
            x = -(1.0D - m);
            z = m;
        }

        double multV2 = yaw % 90.0D;

        if (!(multV2 < 45.0D) && multV2 > 45.0D) {
            multV2 = 45.0D - (multV2 - 45.0D);
        }
        double multV3 = 1.0D + multV2 / 90.0D;
        Vec3 lookV = new Vec3(x * multV3, 0.0D, z * multV3);

        Vec3 additive = lookV;

        for (int counter = 0; counter <= 100; counter++) {
            double negative = player.getRandom().nextDouble() >= 0.5D ? -1.0D : 1.0D;
            Vec3 finalVec = EntityBabylonWeapon.rotateAroundY(lookV, 80.0D * negative);
            finalVec = finalVec.scale(2.0D + player.getRandom().nextDouble() * 10.0D);
            finalVec = finalVec.add(additive.scale(player.getRandom().nextDouble()));
            finalVec = finalVec.add(0.0D, -0.5D + player.getRandom().nextDouble() * 8.0D, 0.0D);
            finalVec = finalVec.add(playerCenter);

            AABB occupied = new AABB(finalVec, finalVec).inflate(AVOID_RADIUS);
            if (!level.getEntitiesOfClass(EntityBabylonWeapon.class, occupied).isEmpty()) {
                continue;
            }
            lookV = finalVec;
            break;
        }

        EntityBabylonWeapon weapon = new EntityBabylonWeapon(level, player);
        weapon.setPos(lookV.x, lookV.y, lookV.z);

        weapon.setYRot(player.getYHeadRot());

        weapon.setRotation(Mth.wrapDegrees(-player.getYHeadRot() + 180.0F));
        weapon.setVariety(player.getRandom().nextInt(12));
        weapon.setDelay(0);
        level.addFreshEntity(weapon);

        SoundHelper.play(
                level,
                weapon.getX(),
                weapon.getY(),
                weapon.getZ(),
                BotaniaSounds.TREASURE_WEAPON_SPAWN,
                SoundSource.PLAYERS,
                1.0F,
                1.0F + player.getRandom().nextFloat() * 3.0F);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemApotheosis1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemApotheosis2.lore"));
        tooltip.add(Component.translatable("item.ItemApotheosis3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable("item.ItemApotheosis4.lore", (int)
                FRConfig.APOTHEOSIS_DIRECT_DAMAGE.get().doubleValue()));

        tooltip.add(Component.translatable("item.ItemApotheosis5.lore", (int)
                FRConfig.APOTHEOSIS_IMPACT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemApotheosis6.lore"));
    }
}
