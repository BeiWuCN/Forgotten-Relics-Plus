package com.beiwu.forgottenrelicsplus.network;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelicsplus.items.ItemTelekinesisTome;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TelekinesisLeftClickPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TelekinesisLeftClickPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "telekinesis_left_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TelekinesisLeftClickPayload> CODEC =
            StreamCodec.of((buffer, payload) -> {
            }, buffer -> new TelekinesisLeftClickPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TelekinesisLeftClickPayload payload, IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemTelekinesisTome.onServerLeftClick(player);

                ItemDimensionalMirror.onServerLeftClick(player);
            }
        });
    }
}
