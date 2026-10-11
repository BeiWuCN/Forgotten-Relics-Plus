package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.items.ItemTelekinesisTome;
import com.beiwu.forgottenrelicsplus.network.TelekinesisLeftClickPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

        if (!(player.getMainHandItem().getItem() instanceof ItemTelekinesisTome)) {
            return;
        }
        PacketDistributor.sendToServer(new TelekinesisLeftClickPayload());
    }

    private FRClientEvents() {
    }
}
