package com.beiwu.forgottenrelicsplus.utils;

import com.beiwu.forgottenrelicsplus.api.OmegaDeathState;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.items.ItemOmegaCore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

public final class OmegaCoreProtection {
    private OmegaCoreProtection() {}

    public static boolean belowVoidLimit(double y, int minimumBuildHeight) {
        return y < (double) minimumBuildHeight - 256.0D;
    }

    public static boolean needsHealthRepair(float health) {
        return !Float.isFinite(health) || health <= 0.0F;
    }

    public static boolean blocksRemoval(Entity.RemovalReason reason) {
        return reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED;
    }

    public static boolean isCarriedBy(Entity entity) {
        return entity instanceof Player player && !player.isRemoved() && player.getInventory() != null
                && FRCarriedItems.anyMatch(player, stack -> stack.getItem() instanceof ItemOmegaCore);
    }

    public static boolean protects(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide() || player.isRemoved()
                || player.getInventory() == null) return false;
        boolean enabled = FRConfig.SPEC.isLoaded()
                ? FRConfig.OMEGA_CORE_PREVENT_DEATH.get() : FRConfig.OMEGA_CORE_PREVENT_DEATH.getDefault();
        if (!enabled || !isCarriedBy(player)) return false;
        if (belowVoidLimit(player.getY(), player.level().getMinBuildHeight())) {
            if (player instanceof OmegaDeathState state && state.forgottenRelicsPlus$canNotifyRevocation(player.tickCount)) {
                player.displayClientMessage(Component.translatable("message.forgotten_relics_plus.omega_revoked"), true);
            }
            return false;
        }
        return true;
    }

    public static boolean preventDeath(Entity entity) {
        if (!protects(entity)) return false;
        restore((Player) entity);
        return true;
    }

    public static void restore(Player player) {
        if (player instanceof OmegaDeathState state) {
            state.forgottenRelicsPlus$restoreLife();
        }
    }
}
