package com.beiwu.forgottenrelicsplus.network;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.items.ItemRingOfDiscord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleDiscordPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleDiscordPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "toggle_discord"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleDiscordPayload> CODEC =
            StreamCodec.of((buffer, payload) -> {
            }, buffer -> new ToggleDiscordPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleDiscordPayload payload, IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemRingOfDiscord.triggerTome(player);
            }
        });
    }
}
