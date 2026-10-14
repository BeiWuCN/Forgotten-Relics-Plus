package com.beiwu.forgottenrelicsplus.events;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.FRCarriedItems;
import com.beiwu.forgottenrelicsplus.utils.FRWornItems;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FRPlayerEvents {
    private FRPlayerEvents() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        CooldownHelper.tick(player);
        ItemTelekinesisTome.tickState(player);
        FRCarriedItems.forEach(player, stack -> {
            if (stack.getItem() instanceof FRRechargable rechargeable) {
                rechargeable.syncChargeProfile(stack, player);
            }
        });
        FRWornItems.forEach(player, stack -> {
            if (stack.getItem() instanceof WearerTickBehaviour behaviour) {
                behaviour.onWearerTick(player, stack);
            }
        });
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerState(event.getEntity());
    }

    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        ItemTelekinesisTome.clearTargetState(event.getEntity());
    }

    public static void onClone(PlayerEvent.Clone event) {
        clearPlayerState(event.getOriginal());
        clearPlayerState(event.getEntity());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        ItemTelekinesisTome.clearStates();
        CooldownHelper.clearAll();
    }

    private static void clearPlayerState(Player player) {
        ItemTelekinesisTome.clearState(player);
        CooldownHelper.clear(player);
    }
}
