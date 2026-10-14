package com.beiwu.forgottenrelicsplus.events;

import com.beiwu.forgottenrelicsplus.api.BreakSpeedBehaviour;
import com.beiwu.forgottenrelicsplus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelicsplus.utils.FRCarriedItems;
import com.beiwu.forgottenrelicsplus.utils.FRWornItems;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class FRInteractionEvents {
    private FRInteractionEvents() {}

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        FRCarriedItems.forEachHeld(player, stack -> {
            if (!event.isCanceled() && stack.getItem() instanceof WeaponAttackBehaviour behaviour) {
                behaviour.onAttackEntity(event, player, stack);
            }
        });
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        float[] boost = {0.0F};
        FRWornItems.forEach(player, stack -> {
            if (stack.getItem() instanceof BreakSpeedBehaviour behaviour) {
                boost[0] += behaviour.breakSpeedBoost(player, stack);
            }
        });
        if (boost[0] != 0.0F) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + boost[0]));
        }
    }
}
