package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemGhastlySkull extends FRItem implements FRRechargable, IWarpingGear {

    public ItemGhastlySkull(Properties properties) {
        super(properties);
    }

    public int getVisCost() {
        return FRConfig.GHASTLY_SKULL_VIS_COST.get();
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.GHASTLY_SKULL_MAX_CHARGE.get()
                : FRConfig.GHASTLY_SKULL_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.GHASTLY_SKULL_WARP.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        float before = player.getHealth();
        if (before < FRConfig.GHASTLY_SKULL_MIN_HEALTH.get()) {
            player.displayClientMessage(Component.translatable("item.ItemGhastlySkullFail.lore"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            player.displayClientMessage(Component.translatable("item.ItemGhastlySkullNoVis.lore"), true);
            return InteractionResultHolder.fail(stack);
        }

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 center = findImpactPoint(level, player, eye, FRConfig.GHASTLY_SKULL_BURST_RANGE.get());

        float sacrificed = Math.max(0.0F, before - 1.0F);
        player.setHealth(1.0F);
        player.invulnerableTime = 20;

        float damage = (float) Math.min(
                sacrificed * FRConfig.GHASTLY_SKULL_DAMAGE_MULT.get(), FRConfig.GHASTLY_SKULL_MAX_DAMAGE.get());
        int hits = detonate(level, player, center, damage);

        float wanted = hits * FRConfig.GHASTLY_SKULL_HEAL_PER_TARGET.get().floatValue();
        if (wanted > 0.0F) {
            player.heal(Math.min(wanted, before - player.getHealth()));
        }

        SoundHelper.play(
                level, center.x, center.y, center.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.WITHER_HURT,
                SoundSource.PLAYERS,
                0.6F,
                0.6F);
        CooldownHelper.setCooldown(player, FRConfig.GHASTLY_SKULL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    private static Vec3 findImpactPoint(Level level, Player player, Vec3 eye, int range) {
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        BlockHitResult hit =
                level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    private static int detonate(Level level, Player caster, Vec3 center, float damage) {
        double radius = FRConfig.GHASTLY_SKULL_BURST_RADIUS.get();
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets =
                level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != caster && entity.isAlive());
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        int hits = 0;
        for (LivingEntity target : targets) {
            if (target.distanceToSqr(center) > radius * radius) {
                continue;
            }
            if (target.hurt(FRDamageTypes.source(level, FRDamageTypes.SOUL_DRAIN), damage)) {
                hits++;
            }
            target.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    FRConfig.GHASTLY_SKULL_WITHER_DURATION.get(),
                    FRConfig.GHASTLY_SKULL_WITHER_LEVEL.get(),
                    true,
                    false));
        }

        for (double t = 0.0D; t <= 1.0D; t += 0.1D) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    caster.getX() + (center.x - caster.getX()) * t,
                    caster.getY() + 1.0D + (center.y - caster.getY()) * t,
                    caster.getZ() + (center.z - caster.getZ()) * t,
                    2,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.01D);
        }
        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x,
                center.y,
                center.z,
                60,
                radius * 0.4D,
                radius * 0.4D,
                radius * 0.4D,
                0.05D);
        serverLevel.sendParticles(
                ParticleTypes.SOUL,
                center.x,
                center.y,
                center.z,
                24,
                radius * 0.3D,
                radius * 0.3D,
                radius * 0.3D,
                0.02D);
        return hits;
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemGhastlySkull1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemGhastlySkull2.lore", getVisCost()));
    }
}
