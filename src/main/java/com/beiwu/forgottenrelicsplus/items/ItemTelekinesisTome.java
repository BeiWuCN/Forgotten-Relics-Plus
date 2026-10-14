package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.beiwu.forgottenrelicsplus.utils.TimedTargetState;
import com.beiwu.forgottenrelicsplus.utils.TimedTargetStateStore;
import com.beiwu.forgottenrelicsplus.utils.TelekinesisTargeting;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityThaumaturgeBoss;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import vazkii.botania.common.entity.GaiaGuardianEntity;

public class ItemTelekinesisTome extends FRItem implements FRRechargable, IWarpingGear, WeaponAttackBehaviour {

    private static final double SEARCH_RANGE = 3.0D;

    private static final double EXISTING_RANGE = 6.0D;

    private static final double HOLD_DISTANCE = 7.5D;

    private static final double LIGHTNING_RANGE = 16.0D;

    private static final int USE_DURATION = 72000;

    private static final int VIS_INTERVAL = 20;

    private static final TimedTargetStateStore TOME_STATES = new TimedTargetStateStore();

    public ItemTelekinesisTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getControlVisCost() {
        return (int) (FRConfig.TOME_OF_PREDESTINY_CONTROL_VIS_COST.get() * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    public int getLightningVisCost() {
        return (int)
                (FRConfig.TOME_OF_PREDESTINY_LIGHTNING_VIS_COST.get() * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    public int getShoveVisCost() {
        return (int) (FRConfig.TOME_OF_PREDESTINY_SHOVE_VIS_COST.get() * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.TOME_OF_PREDESTINY_MAX_CHARGE.get()
                : FRConfig.TOME_OF_PREDESTINY_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_PREDESTINY_WARP.get();
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

        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }
        int elapsed = getUseDuration(stack, player) - remainingUseDuration;
        controlTick(level, player, stack, elapsed);
    }

    public static void tickState(Player player) {
        TOME_STATES.tick(player.getUUID());
    }

    public static void clearState(Player player) {
        TOME_STATES.remove(player.getUUID());
    }

    public static void clearTargetState(Player player) {
        TOME_STATES.clearTarget(player.getUUID());
    }

    public static void clearStates() {
        TOME_STATES.clear();
    }

    @Override
    public void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack) {

        if (attacker.getMainHandItem() != stack) {
            return;
        }
        leftClick(attacker, stack);
    }

    public static void onServerLeftClick(Player player) {
        if (player.getMainHandItem().getItem() instanceof ItemTelekinesisTome tome) {
            tome.leftClick(player, player.getMainHandItem());
        }
    }

    private void leftClick(Player player, ItemStack stack) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }
        TimedTargetState state = state(player);

        if (state.target == -1) {
            return;
        }

        LivingEntity target = getExistingTarget(player, level, state.target, EXISTING_RANGE);
        if (target == null) {
            return;
        }
        lightningAttack(player, target, stack, level);
    }

    private void controlTick(Level level, Player player, ItemStack stack, int elapsed) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        TimedTargetState state = state(player);

        if (state.ticksCooldown != 0) {
            return;
        }

        LivingEntity target = null;
        if (state.target != -1) {
            target = getExistingTarget(player, level, state.target, EXISTING_RANGE);
        }
        if (target == null) {
            target = searchForTarget(player, level, SEARCH_RANGE);
        }

        double length = HOLD_DISTANCE;
        double reDist = state.reDist;
        if (target != null && reDist == -1.0D) {

            reDist = player.distanceTo(target);
        }

        if (target == null) {
            return;
        }

        if (elapsed % VIS_INTERVAL == 0 && !RechargeAccess.consumeCharge(stack, player, getControlVisCost())) {
            player.stopUsingItem();
            return;
        }

        target.resetFallDistance();

        if (target.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 3, true, true));
        }

        Vec3 look = player.getLookAngle();
        Vec3 targetPoint = centerOf(player);
        if (player.isShiftKeyDown()) {
            targetPoint = targetPoint.add(look.scale(reDist));
        } else {
            targetPoint = targetPoint.add(look.scale(length));
            reDist = player.distanceTo(target);
        }

        targetPoint = targetPoint.add(0.0D, 0.5D, 0.0D);

        Vec3 entityCenter = centerOf(target);
        telekinesisParticles(server, entityCenter);

        double multiplier = target.position().distanceTo(targetPoint);
        float vectorPower = 0.66666F;
        if (multiplier < 1.5D) {
            vectorPower = 0.333333F;
        } else if (multiplier >= 8.0D) {
            vectorPower *= (float) (multiplier / 8.0D);
        }

        if (isBlacklistedFromTelekinesis(target)) {
            return;
        }

        setEntityMotionFromVector(target, targetPoint, vectorPower);
        state.target = target.getId();
        state.dist = length;
        state.reDist = reDist;
        state.ticksTillExpire = 5;
    }

    private static LivingEntity searchForTarget(Player player, Level level, double range) {
        return TelekinesisTargeting.findTarget(level, player, centerOf(player), player.getLookAngle(), range);
    }

    private static LivingEntity getExistingTarget(Player player, Level level, int targetId, double range) {
        Entity tracked = level.getEntity(targetId);
        if (!(tracked instanceof LivingEntity target) || target == player || target.isRemoved() || target.isSpectator()) {
            return null;
        }
        return TelekinesisTargeting.isInReach(centerOf(player), player.getLookAngle(), target.getBoundingBox(), range)
                ? target
                : null;
    }

    private static void setEntityMotionFromVector(Entity entity, Vec3 originalPos, float modifier) {
        Vec3 entityVector = centerOf(entity);
        Vec3 finalVector = originalPos.subtract(entityVector);
        if (finalVector.length() > 1.0D) {
            finalVector = finalVector.normalize();
        }
        entity.setDeltaMovement(finalVector.x * modifier, finalVector.y * modifier, finalVector.z * modifier);

        entity.hurtMarked = true;
    }

    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    private static boolean isBlacklistedFromTelekinesis(LivingEntity entity) {
        return entity instanceof EntityThaumaturgeBoss || entity instanceof GaiaGuardianEntity;
    }

    private static void telekinesisParticles(ServerLevel level, Vec3 center) {
        FRParticles.serverWispBurst(
                level,
                center.x,
                center.y,
                center.z,
                0.2F + level.random.nextFloat() * 0.3F,
                0.0F,
                0.5F + level.random.nextFloat() * 0.2F,
                0.2F + level.random.nextFloat() * 0.1F,
                1.0F,
                2,
                0.0D,
                0.15D / Math.sqrt(12.0D));
        level.sendParticles(
                ParticleTypes.PORTAL, center.x, center.y, center.z, 4, 0.0D, 0.0D, 0.0D, 3.0D / Math.sqrt(12.0D));
    }

    private void lightningAttack(Player player, LivingEntity target, ItemStack stack, Level level) {

        if (level.isClientSide() || CooldownHelper.isOnCooldown(player)) {
            return;
        }
        Vec3 targetCenter = centerOf(target);

        Vec3 moveVector = player.getLookAngle().normalize();

        double distance = player.distanceTo(target);
        if (distance <= LIGHTNING_RANGE && RechargeAccess.consumeCharge(stack, player, getLightningVisCost())) {
            if (level instanceof ServerLevel server) {

                double width = 0.225D + player.distanceToSqr(target) / 2000.0D;
                FRBoltParticleData.broadcast(
                        server,
                        player.position().add(0.0D, 1.0D, 0.0D),
                        targetCenter,
                        (float) width,
                        4,
                        FRBoltParticleData.ARC_RED,
                        FRBoltParticleData.ARC_GREEN,
                        FRBoltParticleData.ARC_BLUE);
            }

            SoundHelper.play(
                    level,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.8F);
            double min = FRConfig.TOME_OF_PREDESTINY_DAMAGE_MIN.get();
            double max = FRConfig.TOME_OF_PREDESTINY_DAMAGE_MAX.get();
            target.hurt(FRDamageTypes.source(level, FRDamageTypes.TRUE_LIGHTNING, player), (float)
                    (min + level.random.nextDouble() * (max - min)));
        }

        if (player.isShiftKeyDown() && RechargeAccess.consumeCharge(stack, player, getShoveVisCost())) {

            target.setDeltaMovement(moveVector.x * 3.0D, moveVector.y * 1.5D, moveVector.z * 3.0D);
            target.hurtMarked = true;
            TimedTargetState state = state(player);
            state.clearTarget();
            state.ticksCooldown = FRConfig.TOME_OF_PREDESTINY_SHOVE_COOLDOWN.get();
        }

        CooldownHelper.setCooldown(player, FRConfig.TOME_OF_PREDESTINY_COOLDOWN.get());
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static TimedTargetState state(Player player) {
        return TOME_STATES.getOrCreate(player.getUUID());
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.PredestinyTome1.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome2.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome4.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome5.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome7.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome8.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome10.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable("item.PredestinyTome11_1.lore")
                .append(Component.literal(" "
                        + (int) FRConfig.TOME_OF_PREDESTINY_DAMAGE_MIN.get().doubleValue() + "-"
                        + (int) FRConfig.TOME_OF_PREDESTINY_DAMAGE_MAX.get().doubleValue() + " "))
                .append(Component.translatable("item.PredestinyTome11_2.lore")));
    }
}
