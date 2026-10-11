package com.beiwu.forgottenrelicsplus;

import com.beiwu.forgottenrelicsplus.api.AllyProtectionBehaviour;
import com.beiwu.forgottenrelicsplus.api.BreakSpeedBehaviour;
import com.beiwu.forgottenrelicsplus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID)
public final class FRCommonEvents {

    @SubscribeEvent
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

    @SubscribeEvent
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

    @SubscribeEvent
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

    @SubscribeEvent
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

    @SubscribeEvent
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

    private record Active(IncomingDamageBehaviour behaviour, ItemStack stack) {}

    private record Ally(AllyProtectionBehaviour behaviour, Player guardian, ItemStack stack) {}

    private record Guard(DeathPreventionBehaviour behaviour, ItemStack stack) {}

    private record Carried(CarriedDamageBehaviour behaviour, ItemStack stack) {}

    private enum CarriedSide {
        ATTACK,
        DEFEND,
        HURT
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerState(event.getEntity());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        ItemTelekinesisTome.clearTargetState(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        clearPlayerState(event.getOriginal());
        clearPlayerState(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ItemTelekinesisTome.clearStates();
        CooldownHelper.clearAll();
    }

    private static void clearPlayerState(Player player) {
        ItemTelekinesisTome.clearState(player);
        CooldownHelper.clear(player);
    }

    private FRCommonEvents() {}
}
