package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemTerrorCrown extends ArmorItem implements ICurioItem, IWarpingGear, WearerTickBehaviour {

    public ItemTerrorCrown(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties.component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE));
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TERROR_CROWN_WARP.get();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.isEnchanted()) {
            stack.remove(DataComponents.ENCHANTMENTS);
        }
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        tickCrown(stack, wearer);
    }

    private void tickCrown(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof Player player)) {
            return;
        }
        int interval = FRConfig.TERROR_CROWN_HAVOC_INTERVAL.get();
        if (Math.floorMod(player.tickCount + player.getId(), interval) == 0) {
            cryHavoc(player);
        }
        applyGazeDebuffs(player);
        repairWithMana(stack, player);
    }

    private static void cryHavoc(Player player) {
        int range = FRConfig.TERROR_CROWN_HAVOC_RANGE.get();
        if (range <= 0) {
            return;
        }
        List<net.minecraft.world.entity.Mob> mobs = player.level()
                .getEntitiesOfClass(
                        net.minecraft.world.entity.Mob.class,
                        player.getBoundingBox().inflate(range),
                        mob -> mob.isAlive() && !mob.isRemoved());
        if (mobs.size() <= 1) {
            return;
        }
        for (net.minecraft.world.entity.Mob mob : mobs) {
            vazkii.botania.common.block.block_entity.flower.functional.HeiseiDreamBlockEntity.brainwashEntity(
                    mob, mobs);
        }
    }

    private static void applyGazeDebuffs(Player player) {
        double range = FRConfig.TERROR_CROWN_SCAN_RANGE.get();
        if (range <= 0.0D) {
            return;
        }
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        AABB search = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0D);
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player, eye, end, search, target -> target instanceof LivingEntity && target != player, range * range);
        if (!(hit != null && hit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        target.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS, FRConfig.TERROR_CROWN_BLINDNESS_DURATION.get(), 2, true, false));

        if (!target.hasEffect(MobEffects.WITHER)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    FRConfig.TERROR_CROWN_WITHER_DURATION.get(),
                    FRConfig.TERROR_CROWN_WITHER_LEVEL.get(),
                    false,
                    false));
        }
        target.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION,
                FRConfig.TERROR_CROWN_NAUSEA_DURATION.get(),
                FRConfig.TERROR_CROWN_NAUSEA_LEVEL.get(),
                true,
                false));
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                FRConfig.TERROR_CROWN_SLOWNESS_DURATION.get(),
                FRConfig.TERROR_CROWN_SLOWNESS_LEVEL.get(),
                true,
                false));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                FRConfig.TERROR_CROWN_WEAKNESS_DURATION.get(),
                FRConfig.TERROR_CROWN_WEAKNESS_LEVEL.get(),
                true,
                false));
    }

    private static void repairWithMana(ItemStack stack, Player player) {
        if (!stack.isDamaged()) {
            return;
        }
        int cost = FRConfig.TERROR_CROWN_MANA_COST.get();
        if (vazkii.botania.api.mana.ManaItemHandler.instance().requestManaExact(stack, player, cost, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FRItem.appendShift(tooltip, () -> {
            tooltip.add(Component.translatable("item.ItemTerrorCrown1.lore", FRConfig.TERROR_CROWN_HAVOC_RANGE.get()));
            tooltip.add(Component.translatable("item.ItemTerrorCrown2.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemTerrorCrown3.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemTerrorCrown4.lore"));
        });
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {

        LivingEntity wearer = slotContext.entity();
        return wearer == null
                || !wearer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)
                        .is(this);
    }
}
