package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntitySoulEnergy;
import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
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
import vazkii.botania.common.handler.BotaniaSounds;

public class ItemSoulTome extends FRItem implements FRRechargable, IWarpingGear {

    private static final int USE_DURATION = 72000;

    private static final int SEARCH_RANGE = 20;

    private static final double KNOCKBACK_RANGE = 3.0D;

    private static final float SOUL_HEAL = 1.0F;
    private static final int SOUL_FOOD = 1;
    private static final float SOUL_FOOD_SATURATION = 1.0F;

    private static final double BOUNDARY_HORIZONTAL_MOTION = 0.15D;

    private static final double BOUNDARY_VERTICAL_MOTION = 0.35D;

    private static final float BOUNDARY_INNER_RED = 1.0F;
    private static final float BOUNDARY_INNER_GREEN = 0.9F;
    private static final float BOUNDARY_INNER_BLUE = 0.9F;
    private static final float BOUNDARY_INNER_SIZE = 2.0F;

    private static final int DRAIN_INTERVAL = 4;

    public ItemSoulTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.SOUL_TOME_VIS_COST.get() * FRConfig.SOUL_TOME_VIS_MULT.get());
    }

    public int getKnockbackVisCost() {
        return (int) (FRConfig.SOUL_TOME_KNOCKBACK_VIS_COST.get() * FRConfig.SOUL_TOME_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.SOUL_TOME_MAX_CHARGE.get()
                : FRConfig.SOUL_TOME_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.SOUL_TOME_WARP.get();
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

        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, motion.y, 0.0D);

        if (level.isClientSide()) {
            renderAuraBoundary(player);
            return;
        }

        int warmup = FRConfig.SOUL_TOME_WARMUP_TICKS.get();
        if (remainingUseDuration <= 0 || remainingUseDuration >= USE_DURATION - warmup) {
            return;
        }

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(SEARCH_RANGE),
                entity -> entity != player && entity.isAlive());

        for (LivingEntity target : entities) {
            if (player.distanceTo(target) <= KNOCKBACK_RANGE
                    && RechargeAccess.consumeCharge(stack, player, getKnockbackVisCost())) {
                knockBack(level, player, target);
            }
        }

        if (remainingUseDuration % DRAIN_INTERVAL == 0) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return;
            }
            if (entities.isEmpty()) {
                return;
            }

            LivingEntity victim = entities.get((int) (entities.size() * level.random.nextDouble()));
            drainSoul(level, player, victim);
        }
    }

    private static void knockBack(Level level, Player player, LivingEntity target) {

        Vec3 entityVec = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 playerVec = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        double distance = player.distanceTo(target);

        Vec3 diff = entityVec.subtract(playerVec).scale(1.0D / distance * 3.0D);

        if (level instanceof ServerLevel server) {

            FRBoltParticleData.broadcast(
                    server,
                    player.position().add(0.0D, 1.0D, 0.0D),
                    entityVec,
                    FRBoltParticleData.WIDTH_MAIN,
                    4,
                    FRBoltParticleData.ARC_RED,
                    FRBoltParticleData.ARC_GREEN,
                    FRBoltParticleData.ARC_BLUE);

            SoundHelper.play(
                    level,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.8F);
        }

        double min = FRConfig.SOUL_TOME_KNOCKBACK_DAMAGE_MIN.get();
        double max = FRConfig.SOUL_TOME_KNOCKBACK_DAMAGE_MAX.get();
        target.hurt(FRDamageTypes.source(level, FRDamageTypes.TRUE_LIGHTNING, player), (float)
                (min + level.random.nextDouble() * (max - min)));

        target.setDeltaMovement(diff.x, diff.y + 1.0D, diff.z);
    }

    private static void drainSoul(Level level, Player player, LivingEntity victim) {
        float soulDamage =
                victim.getMaxHealth() / (float) FRConfig.SOUL_TOME_DIVISOR.get().doubleValue();
        float min = FRConfig.SOUL_TOME_DAMAGE_MIN.get().floatValue();
        float max = FRConfig.SOUL_TOME_DAMAGE_MAX.get().floatValue();
        if (soulDamage > max) {
            soulDamage = max;
        } else if (soulDamage < min) {
            soulDamage = min;
        }
        victim.hurt(FRDamageTypes.source(level, FRDamageTypes.SOUL_DRAIN, player), soulDamage);
        spawnSoul(level, victim, player);
    }

    private static void spawnSoul(Level level, LivingEntity source, LivingEntity target) {
        if (level.isClientSide()) {
            return;
        }

        Vec3 center = source.position().add(0.0D, source.getBbHeight() * 0.5D, 0.0D);
        Vec3 look = source.getLookAngle();
        Vec3 spawn = center.add(look.scale(1.0D)).add(0.0D, 0.5D, 0.0D);

        EntitySoulEnergy orb = new EntitySoulEnergy(level, source, target);
        orb.setPos(spawn.x, spawn.y, spawn.z);
        orb.setDeltaMovement(look.scale(1.25D));

        SoundHelper.play(
                level,
                source.getX(),
                source.getY(),
                source.getZ(),
                BotaniaSounds.MISSILE,
                SoundSource.PLAYERS,
                2.0F,
                0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    private static void renderAuraBoundary(Player player) {
        Level level = player.level();
        double y = player.getY();

        for (int i = 0; i < 360; i += 8) {
            double rad = Math.toRadians(i);
            FRParticles.wisp(
                    level,
                    player.getX() - Math.cos(rad) * SEARCH_RANGE,
                    y,
                    player.getZ() - Math.sin(rad) * SEARCH_RANGE,
                    1.0F,
                    1.0F,
                    1.0F,
                    0.5F,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_HORIZONTAL_MOTION,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_VERTICAL_MOTION,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_HORIZONTAL_MOTION,
                    0.8F);
        }

        for (int i = 0; i < 360; i += 16) {
            double rad = Math.toRadians(i);
            FRParticles.sparkle(
                    level,
                    player.getX() - Math.cos(rad) * KNOCKBACK_RANGE,
                    y,
                    player.getZ() - Math.sin(rad) * KNOCKBACK_RANGE,
                    BOUNDARY_INNER_RED,
                    BOUNDARY_INNER_GREEN,
                    BOUNDARY_INNER_BLUE,
                    BOUNDARY_INNER_SIZE,
                    4);
        }
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemSoulTome1.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome2.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome4.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome6.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome7.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome8.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome9.lore"));
    }
}
