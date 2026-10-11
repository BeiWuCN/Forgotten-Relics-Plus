package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityCrimsonOrb;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemCrimsonSpell extends FRItem implements FRRechargable, IWarpingGear {

    private static final int SCAN_RANGE = 32;

    private static final double BASE_BOX = 3.0D;

    public ItemCrimsonSpell(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.CRIMSON_SPELL_VIS_COST.get() * FRConfig.CRIMSON_SPELL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.CRIMSON_SPELL_MAX_CHARGE.get()
                : FRConfig.CRIMSON_SPELL_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.CRIMSON_SPELL_WARP.get();
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

        LivingEntity target = findTarget(level, player);
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return InteractionResultHolder.fail(stack);
        }
        spawnOrb(level, player, target);
        CooldownHelper.setCooldown(player, FRConfig.CRIMSON_SPELL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    private static LivingEntity findTarget(Level level, Player player) {
        Vec3 look = player.getLookAngle();

        Vec3 cursor = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        List<LivingEntity> found = List.of();
        for (int distance = 1; found.isEmpty() && distance < SCAN_RANGE; distance++) {

            double extra = distance > 20 ? 5.0D : (distance > 10 ? 3.0D : 0.0D);
            cursor = cursor.add(look.scale(distance)).add(0.0D, 0.5D, 0.0D);
            double half = BASE_BOX + extra;
            AABB box = new AABB(
                    cursor.x - half,
                    cursor.y - half,
                    cursor.z - half,
                    cursor.x + half,
                    cursor.y + half,
                    cursor.z + half);
            found = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
        }
        if (!found.isEmpty()) {
            return pick(found, level);
        }

        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        player.getX() - SCAN_RANGE,
                        player.getY() - SCAN_RANGE,
                        player.getZ() - SCAN_RANGE,
                        player.getX() + SCAN_RANGE,
                        player.getY() + SCAN_RANGE,
                        player.getZ() + SCAN_RANGE),
                e -> e != player && e.isAlive() && e.hasLineOfSight(player));
        return nearby.isEmpty() ? null : pick(nearby, level);
    }

    private static LivingEntity pick(List<LivingEntity> candidates, Level level) {
        return candidates.get((int) ((candidates.size() - 1) * level.random.nextDouble()));
    }

    private static void spawnOrb(Level level, Player player, LivingEntity target) {
        Vec3 look = player.getLookAngle();
        Vec3 spawn = player.position()
                .add(0.0D, player.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.0D))
                .add(0.0D, 0.5D, 0.0D);
        EntityCrimsonOrb orb = new EntityCrimsonOrb(level, player, target);
        orb.setPos(spawn.x, spawn.y, spawn.z);
        orb.setDeltaMovement(look.scale(0.75D));

        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                0.6F,
                0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemCrimsonSpell1.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell2.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell4.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell5.lore"));

        tooltip.add(Component.translatable(
                "item.ItemCrimsonSpell6.lore",
                (int) FRConfig.CRIMSON_SPELL_DAMAGE_MIN.get().doubleValue(),
                (int) FRConfig.CRIMSON_SPELL_DAMAGE_MAX.get().doubleValue()));
    }
}
