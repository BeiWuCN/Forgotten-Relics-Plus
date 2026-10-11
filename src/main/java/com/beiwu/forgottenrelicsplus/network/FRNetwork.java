package com.beiwu.forgottenrelicsplus.network;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class FRNetwork {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ToggleDiscordPayload.TYPE, ToggleDiscordPayload.CODEC, ToggleDiscordPayload::handle);

        registrar.playToServer(TelekinesisLeftClickPayload.TYPE, TelekinesisLeftClickPayload.CODEC,
                TelekinesisLeftClickPayload::handle);
    }

    private FRNetwork() {
    }
}
