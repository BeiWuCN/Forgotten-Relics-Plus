package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.network.ToggleDiscordPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, value = Dist.CLIENT)
public final class FRKeybinds {

    public static final KeyMapping DISCORD_RING = new KeyMapping(
            "key.discordRing",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_X),
            "key.categories.ForgottenRelicsRE");

    private static boolean wasDown;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean down = DISCORD_RING.isDown();
        if (down && !wasDown) {
            PacketDistributor.sendToServer(new ToggleDiscordPayload());
        }
        wasDown = down;
    }

    private FRKeybinds() {
    }
}
