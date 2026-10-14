package com.beiwu.forgottenrelicsplus.events;

import com.beiwu.forgottenrelicsplus.api.AllyProtectionBehaviour;
import com.beiwu.forgottenrelicsplus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.utils.FRCarriedItems;
import com.beiwu.forgottenrelicsplus.utils.FRWornItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class FRDamageEvents {
    private FRDamageEvents() {}

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        dispatchCarriedBehaviours(event, player);
        if (!event.isCanceled()) {
            dispatchWearerBehaviours(event, player);
        }
        if (!event.isCanceled()) {
            dispatchAllyProtection(event, player);
        }
    }

    private static void dispatchCarriedBehaviours(LivingIncomingDamageEvent event, Player victim) {
        if (event.getSource().getEntity() instanceof Player attacker && attacker != victim) {
            runCarried(event, attacker, CarriedSide.ATTACK, carriedBehaviours(attacker));
        }
        if (event.isCanceled()) {
            return;
        }
        List<Carried> carried = carriedBehaviours(victim);
        runCarried(event, victim, CarriedSide.DEFEND, carried);
        if (event.isCanceled()) {
            return;
        }
        runCarried(event, victim, CarriedSide.HURT, carried);
    }

    private static List<Carried> carriedBehaviours(Player carrier) {
        List<Carried> list = new ArrayList<>();
        Set<CarriedDamageBehaviour> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        FRCarriedItems.forEach(carrier, stack -> {
            if (!(stack.getItem() instanceof CarriedDamageBehaviour behaviour)) {
                return;
            }
            if (!seen.add(behaviour)) {
                return;
            }
            list.add(new Carried(behaviour, stack));
        });
        list.sort(Comparator.comparingInt(entry -> entry.behaviour().priority()));
        return list;
    }

    private static void runCarried(
            LivingIncomingDamageEvent event, Player carrier, CarriedSide side, List<Carried> list) {
        for (Carried entry : list) {
            if (event.isCanceled()) {
                return;
            }
            switch (side) {
                case ATTACK -> entry.behaviour().onCarriedAttack(event, carrier, entry.stack());
                case DEFEND -> entry.behaviour().onCarriedDefend(event, carrier, entry.stack());
                case HURT -> entry.behaviour().onCarriedHurt(event, carrier, entry.stack());
            }
        }
    }

    private static void dispatchWearerBehaviours(LivingIncomingDamageEvent event, Player player) {
        List<Active> active = new ArrayList<>();
        FRWornItems.forEach(player, stack -> {
            if (!(stack.getItem() instanceof IncomingDamageBehaviour behaviour)) {
                return;
            }
            for (Active existing : active) {
                if (existing.behaviour() == behaviour) {
                    return;
                }
            }
            active.add(new Active(behaviour, stack));
        });
        if (active.isEmpty()) {
            return;
        }
        active.sort(Comparator.comparingInt(entry -> entry.behaviour().priority()));

        for (Active entry : active) {
            if (event.isCanceled()) {
                return;
            }
            entry.behaviour().onIncomingDamage(event, player, entry.stack());
        }
    }

    private static void dispatchAllyProtection(LivingIncomingDamageEvent event, Player victim) {
        List<Ally> candidates = new ArrayList<>();
        for (Player guardian : victim.level().players()) {
            if (guardian == victim || guardian.isSpectator()) {
                continue;
            }
            FRWornItems.forEach(guardian, stack -> {
                if (stack.getItem() instanceof AllyProtectionBehaviour behaviour
                        && guardian.distanceToSqr(victim) <= behaviour.radius() * behaviour.radius()) {
                    candidates.add(new Ally(behaviour, guardian, stack));
                }
            });
        }
        if (candidates.isEmpty()) {
            return;
        }

        Set<AllyProtectionBehaviour> attempted = new HashSet<>();
        while (!candidates.isEmpty() && !event.isCanceled()) {
            Ally ally = candidates.remove(victim.getRandom().nextInt(candidates.size()));
            if (attempted.add(ally.behaviour())) {
                ally.behaviour().onAllyDamage(event, victim, ally.guardian(), ally.stack());
            }
        }
    }

    private record Active(IncomingDamageBehaviour behaviour, ItemStack stack) {}

    private record Ally(AllyProtectionBehaviour behaviour, Player guardian, ItemStack stack) {}

    private record Carried(CarriedDamageBehaviour behaviour, ItemStack stack) {}

    private enum CarriedSide {
        ATTACK,
        DEFEND,
        HURT
    }
}
