package com.beiwu.forgottenrelicsplus.api;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface FRRechargable {
    int getMaxCharge(ItemStack stack, LivingEntity holder);

    default void syncChargeProfile(ItemStack stack, LivingEntity holder) {
        int capacity = getMaxCharge(stack, holder);
        ChargeProfile profile = stack.get(TTDataComponents.RECHARGEABLE.get());
        if (profile == null || profile.capacity() != capacity) {
            stack.set(
                    TTDataComponents.RECHARGEABLE.get(),
                    new ChargeProfile(capacity, profile == null ? ChargeDisplay.ON_CHANGE : profile.display()));
            int charge = stack.getOrDefault(TTDataComponents.CHARGE.get(), 0);
            if (charge > capacity) {
                stack.set(TTDataComponents.CHARGE.get(), capacity);
            }
        }
    }
}
