package com.beiwu.forgottenrelics_re.network;

import com.beiwu.forgottenrelics_re.ForgottenRelics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络载荷注册。
 *
 * <p>对应 1.12.2 原版 {@code Main} 里那个 {@code SimpleNetworkWrapper} 的初始化与
 * 十几个 {@code registerMessage} 调用。1.21.1 换成了 {@link RegisterPayloadHandlersEvent}：
 * 在模组总线上注册一次，之后每新增一种消息只要多一行 {@code playToServer}。
 *
 * <p>版本号参数是协议版本，客户端/服务端不一致时会明确拒绝连接，这里先统一用 "1"。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class FRNetwork {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ToggleDiscordPayload.TYPE, ToggleDiscordPayload.CODEC, ToggleDiscordPayload::handle);
    }

    private FRNetwork() {
    }
}
