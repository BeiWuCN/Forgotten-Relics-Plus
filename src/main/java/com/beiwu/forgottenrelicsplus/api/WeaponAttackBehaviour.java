package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public interface WeaponAttackBehaviour {

    void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack);
}
