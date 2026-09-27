package com.beiwu.forgottenrelics_plus.utils;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 「玩家身上带着的东西」的遍历入口。
 *
 * <p>与 {@link FRWornItems} 的分工要分清楚，两者语义不同，用错会直接改变游戏行为：
 *
 * <ul>
 *   <li>{@link FRWornItems} 是<b>穿戴着</b>的东西（Curios 饰品栏 + 护甲槽）。所有「佩戴时生效」的
 *       遗物都必须用它——否则把湮灭护符塞进背包也会开始吸收伤害。</li>
 *   <li>本类是<b>带着</b>的东西（整个物品栏 + 饰品栏），对应原版用
 *       {@code player.inventory.hasItem(...)} 判断的那类效果。</li>
 * </ul>
 *
 * <p>原版里这一类效果分两种落地方式，现代写法也分两种：
 * <ul>
 *   <li><b>每 tick 生效</b>的（混沌之核、欧米伽之核、破碎的命运巨著）：原版写在
 *       {@code Item#onUpdate} 里，1.21.1 的对应物就是原生的 {@code Item#inventoryTick}，
 *       <b>不需要经过本类</b>；</li>
 *   <li><b>由事件触发</b>的（致死免死等）：原版写在 {@code RelicsEventHandler} 里翻背包，
 *       1.21.1 由 {@code FRCommonEvents} 用本类派发。</li>
 * </ul>
 */
public final class FRCarriedItems {

    private FRCarriedItems() {
    }

    /**
     * 依次访问玩家随身携带的每一份非空物品（整个物品栏 + Curios 饰品栏）。
     *
     * <p>{@code Inventory#getContainerSize} 已经把主物品栏、护甲槽与副手合并计算，
     * 所以这里不必再单独遍历 {@code armor}。
     */
    public static void forEach(Player player, Consumer<ItemStack> action) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                action.accept(stack);
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                ItemStack stack = equipped.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    action.accept(stack);
                }
            }
        });
    }

    /**
     * 依次访问玩家手里拿着的东西（主手 + 副手）。
     *
     * <p>对应原版 {@code Item#onLeftClickEntity} 的场景——只有拿在手上的那一份才该生效，
     * 背包里再放一把不该叠加上去。
     */
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
