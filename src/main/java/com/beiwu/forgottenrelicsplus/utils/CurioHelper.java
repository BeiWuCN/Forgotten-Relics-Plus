package com.beiwu.forgottenrelicsplus.utils;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public final class CurioHelper {

    private CurioHelper() {
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(entity);
        if (handler.isEmpty()) {
            return false;
        }
        var equipped = handler.get().getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ItemStack> findEquipped(LivingEntity entity, Item item) {
        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(entity);
        if (handler.isEmpty()) {
            return Optional.empty();
        }
        var equipped = handler.get().getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static boolean replaceFirst(LivingEntity entity, Item from, ItemStack replacement) {
        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(entity);
        if (handler.isEmpty()) {
            return false;
        }
        var equipped = handler.get().getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(from)) {
                equipped.setStackInSlot(slot, replacement);
                return true;
            }
        }
        return false;
    }
}
