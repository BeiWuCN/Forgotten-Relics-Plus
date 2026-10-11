package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.FRCarriedItems;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class ItemOmegaCore extends FRItem implements DeathPreventionBehaviour {

    private static final int PRIORITY = 100;

    public ItemOmegaCore(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        int amount = FRConfig.OMEGA_CORE_VIS_PER_TICK.get();
        int recharge = FRConfig.OMEGA_CORE_RECHARGE_PER_TICK.get();
        if (amount <= 0 && recharge <= 0) {
            return;
        }

        if (amount > 0) {
            for (ResourceKey<IAspect> aspect : TTAspects.PRIMALS) {
                ItemStack wand = findWandWithRoom(player, aspect);
                if (wand != null) {
                    WandVisHelper.addVis(wand, aspect, amount, true);
                }
            }
        }

        if (recharge > 0) {
            FRCarriedItems.forEach(player, carried -> {
                if (carried.getItem() instanceof FRRechargable && !(carried.getItem() == this)) {
                    RechargeAccess.rechargeItemBlindly(carried, player, recharge);
                }
            });
        }
    }

    private static ItemStack findWandWithRoom(Player player, ResourceKey<IAspect> aspect) {
        Inventory inventory = player.getInventory();
        List<ItemStack> candidates = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !(candidate.getItem() instanceof ItemWand)) {
                continue;
            }
            if (WandVisHelper.getVis(candidate, aspect) < WandVisHelper.getMaxVis(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {
        if (!FRConfig.OMEGA_CORE_PREVENT_DEATH.get()) {
            return;
        }
        event.setCanceled(true);

        player.setHealth(1.0F);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.OmegaCore1.lore"));
        tooltip.add(Component.translatable("item.OmegaCore2.lore"));
    }
}
