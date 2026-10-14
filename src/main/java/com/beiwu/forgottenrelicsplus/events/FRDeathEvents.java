package com.beiwu.forgottenrelicsplus.events;

import com.beiwu.forgottenrelicsplus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelicsplus.utils.FRCarriedItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class FRDeathEvents {
    private FRDeathEvents() {}

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player victim) {
            for (Guard guard : sortedGuards(victim)) {
                if (event.isCanceled()) {
                    return;
                }
                guard.behaviour().onLethalDamage(event, victim, guard.stack());
            }
            return;
        }
        if (event.getSource().getEntity() instanceof Player attacker) {
            for (Guard guard : sortedGuards(attacker)) {
                if (event.isCanceled()) {
                    return;
                }
                guard.behaviour().onLethalDamageCaused(event, attacker, guard.stack());
            }
        }
    }

    private static List<Guard> sortedGuards(Player player) {
        List<Guard> guards = new ArrayList<>();
        FRCarriedItems.forEach(player, stack -> {
            if (!(stack.getItem() instanceof DeathPreventionBehaviour behaviour)) {
                return;
            }
            for (Guard existing : guards) {
                if (existing.behaviour() == behaviour) {
                    return;
                }
            }
            guards.add(new Guard(behaviour, stack));
        });
        guards.sort(Comparator.comparingInt(entry -> entry.behaviour().priority()));
        return guards;
    }

    private record Guard(DeathPreventionBehaviour behaviour, ItemStack stack) {}
}
