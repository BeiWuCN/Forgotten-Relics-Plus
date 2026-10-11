package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public interface IncomingDamageBehaviour {

    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack);
}
