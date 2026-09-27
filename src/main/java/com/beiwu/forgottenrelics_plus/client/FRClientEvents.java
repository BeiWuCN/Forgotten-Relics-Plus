package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelics_plus.network.TelekinesisLeftClickPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端游戏事件。
 *
 * <p>目前只有一件事：把「预言之典按下左键」报给服务端。
 *
 * <p>1.7.10 原版的做法是物品在客户端 {@code onUpdate} 里轮询攻击键；这里换成两个原版事件：
 * <ul>
 *   <li>{@link PlayerInteractEvent.LeftClickEmpty}：<b>只在客户端触发</b>，左键点空气时；
 *       服务端收不到，必须自己发包；</li>
 *   <li>{@link PlayerInteractEvent.LeftClickBlock}：点方块。客户端在
 *       {@code MultiPlayerGameMode#startAttack} 里 fire 一次（按下边沿，不是持续按住），
 *       服务端那份要等真正发出挖掘动作包才有；这里统一只走「客户端发包」一条路，
 *       避免同一次点击被处理两遍。</li>
 * </ul>
 *
 * <p>左键点到实体的情况由服务端的 {@code AttackEntityEvent} 负责（见
 * {@code FRCommonEvents#onAttackEntity}），与这里互补、不重叠。
 *
 * <p>客户端只判断「主手是不是预言之典」；目标锁定、冷却、充能校验全部在服务端。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class FRClientEvents {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        sendIfHoldingTome(event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        sendIfHoldingTome(event);
    }

    private static void sendIfHoldingTome(PlayerInteractEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // 只认主手：原版 leftClick 看的就是 getHeldItem()。
        if (!(player.getMainHandItem().getItem() instanceof ItemTelekinesisTome)) {
            return;
        }
        PacketDistributor.sendToServer(new TelekinesisLeftClickPayload());
    }

    private FRClientEvents() {
    }
}
