package com.beiwu.forgottenrelics_plus.network;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemRingOfDiscord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 「切换不和谐之戒开关」的客户端→服务端消息。
 *
 * <p>对应 1.12.2 原版的 {@code DiscordKeybindMessage}（一个空的 {@code IMessage}）。
 * 1.21.1 改用 {@link CustomPacketPayload}：定义一个类型标识 + 一个编解码器即可，
 * 因为不带任何数据，编解码器两边都是空实现。
 *
 * <p>真正的切换逻辑放在服务端：客户端只是「按下了键」这个事件的发生地，
 * 而戒指的数据组件必须在权威端改写才会同步回去。
 */
public record ToggleDiscordPayload() implements CustomPacketPayload {

    // 用显式构造器而不是 CustomPacketPayload.createType(String)：
    // 后者会把整串当成 path 并补上 minecraft 命名空间，导致 ResourceLocation 校验失败。
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

    /** 服务端处理：戒指按键 —— 戴着戒指且背包里有错位之典时远程施放它。 */
    public static void handle(ToggleDiscordPayload payload, IPayloadContext context) {
        // 网络线程不能直接碰世界状态，挪到主线程执行。
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemRingOfDiscord.triggerTome(player);
            }
        });
    }
}
