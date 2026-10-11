package com.beiwu.forgottenrelicsplus.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class CooldownHelper {

    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

    private CooldownHelper() {}

    public static void setCooldown(Player player, int ticks) {
        if (ticks <= 0) {
            COOLDOWNS.remove(player.getUUID());
            return;
        }
        COOLDOWNS.put(player.getUUID(), ticks);
    }

    public static boolean isOnCooldown(Player player) {
        Integer remaining = COOLDOWNS.get(player.getUUID());
        return remaining != null && remaining > 0;
    }

    public static void clear(Player player) {
        COOLDOWNS.remove(player.getUUID());
    }

    public static void clearAll() {
        COOLDOWNS.clear();
    }

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
