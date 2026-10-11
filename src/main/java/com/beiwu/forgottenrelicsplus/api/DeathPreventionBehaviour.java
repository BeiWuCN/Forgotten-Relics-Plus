package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public interface DeathPreventionBehaviour {

    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack);

    default void onLethalDamageCaused(LivingDeathEvent event, Player attacker, ItemStack stack) {
    }
}
