package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 「佩戴时每 tick 触发」的行为。
 *
 * <p>取代原版把所有物品的 tick 逻辑塞进一个 {@code RelicsEventHandler} 的写法。
 * 物品自己实现这个接口，由 {@code FRWornItems} 统一派发；新增物品时<b>不需要改任何事件类</b>。
 *
 * <p>派发范围是「玩家身上所有被穿戴的东西」——Curios 饰品栏 + 游戏本体护甲槽，
 * 因此同一件物品无论走哪条途径（例如恐惧之冠既能戴头饰也能当头盔）都只有一份实现。
 */
public interface WearerTickBehaviour {

    /**
     * 只在服务端调用。
     *
     * @param wearer 佩戴者
     * @param stack  正在生效的那一份物品
     */
    void onWearerTick(LivingEntity wearer, ItemStack stack);
}
