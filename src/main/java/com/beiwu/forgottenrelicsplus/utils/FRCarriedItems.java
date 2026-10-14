package com.beiwu.forgottenrelicsplus.utils;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class FRCarriedItems {

    private FRCarriedItems() {
    }

    public static boolean anyMatch(Player player, Predicate<ItemStack> predicate) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack)) return true;
        }
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                ItemStack stack = equipped.getStackInSlot(slot);
                if (!stack.isEmpty() && predicate.test(stack)) return true;
            }
            return false;
        }).orElse(false);
    }

    public static void forEach(Player player, Consumer<ItemStack> action) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                action.accept(stack);
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                ItemStack stack = equipped.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    action.accept(stack);
                }
            }
        });
    }

    public static void forEachHeld(Player player, Consumer<ItemStack> action) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty()) {
            action.accept(main);
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty()) {
            action.accept(off);
        }
    }
}
