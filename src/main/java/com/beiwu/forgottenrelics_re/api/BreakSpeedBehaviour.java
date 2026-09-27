package com.beiwu.forgottenrelics_re.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 「玩家挖掘方块时」的行为。
 *
 * <p>采矿护符的挖掘加速走这里。它与 {@link WearerTickBehaviour} 分开，是因为触发源完全不同：
 * 前者由玩家动作驱动、频率很低，后者每 tick 都跑。
 *
 * <p>刻意做成「返回一个倍率贡献」而不是「直接改事件」：原版
 * {@code RelicsEventHandler.miningStuff} 是先把所有护符的加成<b>相加</b>成一个总倍率，
 * 再一次性乘上去——两枚护符同时佩戴是 {@code 1 + 3 + 1 = 5} 倍，而不是 {@code 4 × 2 = 8} 倍。
 * 若让每件物品各自去乘事件里的速度，就变成相乘了，与原版不符。
 * 派发器负责把同一玩家身上所有贡献相加，物品只管报自己的数。
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
