package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BreakSpeedBehaviour {

    float breakSpeedBoost(Player player, ItemStack stack);
}
