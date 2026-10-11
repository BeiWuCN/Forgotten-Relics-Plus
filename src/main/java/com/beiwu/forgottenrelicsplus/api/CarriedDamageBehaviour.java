package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public interface CarriedDamageBehaviour {

    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    default void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
    }

    default void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }

    default void onCarriedHurt(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }
}
