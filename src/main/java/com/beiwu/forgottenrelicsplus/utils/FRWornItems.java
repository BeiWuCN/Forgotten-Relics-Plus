package com.beiwu.forgottenrelicsplus.utils;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class FRWornItems {

    private FRWornItems() {
    }

    public static void forEach(Player player, Consumer<ItemStack> action) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                ItemStack stack = equipped.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    action.accept(stack);
                }
            }
        });
        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty()) {
                action.accept(armor);
            }
        }
    }
}
