package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 「玩家将要死亡时」的行为。
 *
 * <p>对应 1.7.10 原版在 {@code RelicsEventHandler#onPlayerDeath}（{@code LivingDeathEvent}）里
 * 翻背包找欧米伽之核 / 破碎的命运巨著的那一段。
 *
 * <p>派发范围是<b>随身携带</b>的物品（见 {@code FRCarriedItems}），因为原版就是用
 * {@code player.inventory.hasItem(...)} 判断的，不要求穿戴。
 *
 * <p>多件物品同时生效时按 {@link #priority()} 从小到大依次执行；一旦有物品取消了事件，
 * 后面的就不再收到通知——原版是 if / else if，欧米伽之核优先于命运巨著，这里用优先级表达同一件事。
 */
public interface DeathPreventionBehaviour {

    /** 默认优先级，数值越小越先执行。 */
    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * @param event  可取消（{@code LivingDeathEvent} 实现了 {@code ICancellableEvent}）
     * @param player 将要死亡的玩家
     * @param stack  正在生效的那一份物品
     */
    void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack);
}
