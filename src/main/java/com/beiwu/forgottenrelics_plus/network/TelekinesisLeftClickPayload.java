package com.beiwu.forgottenrelics_plus.network;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemDimensionalMirror;
import com.beiwu.forgottenrelics_plus.items.ItemTelekinesisTome;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 「预言之典按下左键」的客户端→服务端消息（不带数据）。
 *
 * <h2>为什么需要这个包</h2>
 *
 * <p>原版的左键闪电走的是 {@code TelekinesisAttackMessage}：客户端在
 * {@code onUpdate} 里检测 {@code keyBindAttack.isKeyDown()} 的「按下」边沿，然后发包；
 * 服务端 {@code leftClick(player)} 完全不管这次左键有没有点到实体——只要身上锁着目标，
 * 朝空气挥一下就能打雷。
 *
 * <p>1.21.1 里「左键点到实体」有 NeoForge 的 {@code AttackEntityEvent}（服务端事件，
 * 见 {@code WeaponAttackBehaviour} 的派发），但<b>左键点空气只有客户端的
 * {@code PlayerInteractEvent.LeftClickEmpty}</b>，点方块也只有客户端那一份能可靠拿到，
 * 服务端不会为一次空挥收到任何事件。所以照 {@code ToggleDiscordPayload} 的写法补一个空载荷：
 * 客户端在 {@code LeftClickEmpty} / {@code LeftClickBlock} 时发出来，
 * 服务端收到后跑与 {@code AttackEntityEvent} 完全相同的那段逻辑（冷却、锁定、充能校验都在那边）。
 *
 * <p>真正的判定仍然全在服务端：客户端只是「按下了左键」这个事实的发生地。
 */
public record TelekinesisLeftClickPayload() implements CustomPacketPayload {

    // 用显式构造器而不是 CustomPacketPayload.createType(String)：
    // 后者会把整串当成 path 并补上 minecraft 命名空间，导致 ResourceLocation 校验失败。
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

    /** 服务端处理：分发给各个关心左键的物品（预言之典、空间魔镜），由它们自行校验条件。 */
    public static void handle(TelekinesisLeftClickPayload payload, IPayloadContext context) {
        // 网络线程不能直接碰世界状态，挪到主线程执行。
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemTelekinesisTome.onServerLeftClick(player);
                // 同一条左键通道：空间魔镜的"潜行+左键清空坐标"也走这里。
                ItemDimensionalMirror.onServerLeftClick(player);
            }
        });
    }
}
