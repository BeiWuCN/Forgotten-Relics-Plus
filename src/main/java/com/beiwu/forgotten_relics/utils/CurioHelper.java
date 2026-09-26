package com.beiwu.forgottenrelics_re.utils;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * 饰品栏（Curios）辅助方法。
 *
 * <p>1.12.2 原版用的是 Baubles，判断「玩家身上是否戴着某个饰品」靠
 * {@code BaublesApi.isBaubleEquipped(player, item) != -1}（见原版
 * {@code SuperpositionHandler.hasBauble}）。Curios 没有语义完全对等的单方法，但
 * {@code CuriosApi.getCuriosInventory} 能拿到整个「已装备饰品栏」的 {@code IItemHandler}，
 * 直接线性扫描即可，行为与 Baubles 版本一致：只看已装备的，不看背包里的。
 */
public final class CurioHelper {

    private CurioHelper() {
    }

    /**
     * 玩家身上是否装备着指定物品（任意槽位、任意数量）。
     *
     * @param entity 被检查的实体，通常是玩家
     * @param item   要找的物品
     * @return 只要有一个槽位装着该物品就返回 true
     */
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
}
