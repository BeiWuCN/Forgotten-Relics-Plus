package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public interface AllyProtectionBehaviour {

    double radius();

    boolean onAllyDamage(LivingIncomingDamageEvent event, Player victim, Player guardian, ItemStack stack);
}
