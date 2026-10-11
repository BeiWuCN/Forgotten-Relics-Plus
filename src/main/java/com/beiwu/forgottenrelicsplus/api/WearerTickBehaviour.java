package com.beiwu.forgottenrelicsplus.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface WearerTickBehaviour {

    void onWearerTick(LivingEntity wearer, ItemStack stack);
}
