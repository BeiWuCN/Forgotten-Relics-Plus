package com.beiwu.forgottenrelicsplus;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.beiwu.forgottenrelicsplus.events.FRPlayerEvents;
import com.beiwu.forgottenrelicsplus.events.FRDamageEvents;
import com.beiwu.forgottenrelicsplus.events.FRDeathEvents;
import com.beiwu.forgottenrelicsplus.events.FRInteractionEvents;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID)
public final class FRCommonEvents {
    private FRCommonEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        FRPlayerEvents.onPlayerTick(event);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FRPlayerEvents.onLogout(event);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        FRPlayerEvents.onDimensionChange(event);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        FRPlayerEvents.onClone(event);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        FRPlayerEvents.onServerStopped(event);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        FRDamageEvents.onIncomingDamage(event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        FRDeathEvents.onLivingDeath(event);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        FRInteractionEvents.onAttackEntity(event);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        FRInteractionEvents.onBreakSpeed(event);
    }
}
