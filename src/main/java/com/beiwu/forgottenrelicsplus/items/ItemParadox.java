package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class ItemParadox extends SwordItem implements IWarpingGear, WeaponAttackBehaviour {

    private static final int REPAIR_INTERVAL = 20;

    public ItemParadox(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !stack.isDamaged() || entity.tickCount % REPAIR_INTERVAL != 0) {
            return;
        }
        stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack) {
        double cap = FRConfig.PARADOX_DAMAGE_CAP.get();
        if (cap <= 0.0D) {
            return;
        }
        double toTarget = attacker.getRandom().nextDouble() * cap;
        double toSelf = cap - toTarget;

        event.getTarget().hurt(attacker.damageSources().playerAttack(attacker), (float) toTarget);
        attacker.hurt(attacker.damageSources().playerAttack(attacker), (float) toSelf);
        event.setCanceled(true);
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.PARADOX_WARP.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FRItem.appendShift(tooltip, () -> {
            tooltip.add(Component.translatable("item.ItemParadox1.lore"));
            tooltip.add(Component.translatable("item.ItemParadox2.lore"));
            tooltip.add(Component.translatable("item.ItemParadox3.lore"));
            tooltip.add(Component.translatable("item.ItemParadox4.lore"));
            tooltip.add(Component.translatable("item.ItemParadox5.lore"));
        });
        tooltip.add(Component.translatable("item.FREmpty.lore"));

        tooltip.add(Component.translatable("item.ItemParadoxDamage_1.lore")
                .append(String.valueOf(FRConfig.PARADOX_DAMAGE_CAP.get().intValue()))
                .append(Component.translatable("item.ItemParadoxDamage_2.lore")));
    }
}
