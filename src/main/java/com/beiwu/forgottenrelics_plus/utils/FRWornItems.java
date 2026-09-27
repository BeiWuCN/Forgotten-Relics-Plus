package com.beiwu.forgottenrelics_plus.utils;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 「玩家身上所有被穿戴的东西」的统一入口。
 *
 * <p>取代 RE 到处出现的 {@code BaublesApi.getBaublesHandler(player).getStackInSlot(i)} +
 * {@code player.getItemStackFromSlot(...)} 两套写法：饰品栏与护甲槽在这里合并成一条序列，
 * 行为派发只认「谁被穿着」，不关心它挂在哪个系统里。
 *
 * <p>刻意只提供 {@link #forEach} 这种回调式遍历，不返回 List —— 每 tick 派发时不会产生额外分配。
 */
public final class FRWornItems {

    private FRWornItems() {
    }

    /**
     * 依次访问玩家身上每一份非空的可穿戴物品（Curios 饰品栏 + 护甲槽）。
     *
     * <p>回调里拿到的是<b>容器里那一份真实的 ItemStack</b>，可以直接读写数据组件，
     * 改动会落到玩家身上（Curios 的槽位栈是活引用）。
     */
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
