package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 玩家挖掘方块时生效的行为。
 *
 * <p>采矿护符的挖掘加速走这里。它与 {@link WearerTickBehaviour} 分开，因为触发源不同：
 * 前者由玩家动作驱动、频率很低，后者每 tick 都跑。
 *
 * <p>接口只返回「倍率贡献」，由派发器把同一玩家身上所有贡献相加后一次性乘上去。不能改成
 * 「直接改事件」：原版 {@code RelicsEventHandler.miningStuff} 先把所有护符的加成<b>相加</b>
 * 成一个总倍率，两枚护符同时佩戴是 {@code 1 + 3 + 1 = 5} 倍，不是 {@code 4 × 2 = 8} 倍；
 * 若每件物品各自去乘事件里的速度就变成相乘，与原版不符。
 */
public interface BreakSpeedBehaviour {

    /**
     * 返回挖掘速度的<b>额外</b>倍率贡献，{@code 0} 表示无加成。
     *
     * @param player 挖掘者
     * @param stack  正在生效的那一份物品
     */
    float breakSpeedBoost(Player player, ItemStack stack);
}
