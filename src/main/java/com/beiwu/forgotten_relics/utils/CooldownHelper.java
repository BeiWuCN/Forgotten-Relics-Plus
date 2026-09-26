package com.beiwu.forgottenrelics_re.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家冷却计时器。
 *
 * <p>对应 1.12.2 原版 {@code SuperpositionHandler} 里的 {@code setCasted} / {@code isOnCoodown}
 * 一对方法，以及 {@code Main.castingCooldowns} 那张 {@code HashMap<EntityPlayer, Integer>}。
 *
 * <p>原版直接用玩家对象当 key，1.21.1 里改用玩家的 UUID：
 * 玩家的 {@code Player} 实例在维度切换或重登时会被替换，用对象当 key 会漏掉冷却；
 * UUID 在整个账号生命周期内稳定。
 *
 * <p>计时单位是 tick，按服务端 tick 递减。
 */
public final class CooldownHelper {

    /** 玩家 UUID -> 剩余 tick 数。 */
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

    private CooldownHelper() {
    }

    /**
     * 给玩家设置冷却。
     *
     * @param player   目标玩家
     * @param ticks    冷却时长（tick）
     */
    public static void setCooldown(Player player, int ticks) {
        if (ticks <= 0) {
            COOLDOWNS.remove(player.getUUID());
            return;
        }
        COOLDOWNS.put(player.getUUID(), ticks);
    }

    /** 玩家是否仍在冷却中。 */
    public static boolean isOnCooldown(Player player) {
        Integer remaining = COOLDOWNS.get(player.getUUID());
        return remaining != null && remaining > 0;
    }

    /** 每服务端 tick 调用一次，把冷却往前推。 */
    public static void tick(Player player) {
        UUID id = player.getUUID();
        Integer remaining = COOLDOWNS.get(id);
        if (remaining == null || remaining <= 0) {
            return;
        }
        if (remaining == 1) {
            COOLDOWNS.remove(id);
        } else {
            COOLDOWNS.put(id, remaining - 1);
        }
    }
}
