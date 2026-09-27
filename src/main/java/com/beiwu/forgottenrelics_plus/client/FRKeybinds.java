package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.network.ToggleDiscordPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端键位的轮询逻辑，对应 RE 的 {@code RelicsKeybindHandler}。
 *
 * <p>1.21.1 的轮询事件是 {@code ClientTickEvent.Post}，发消息改用 {@code PacketDistributor}。
 * 主键位注册在 {@link FRClientSetup}（模组总线），本类只处理运行期输入；两者挂的总线不同，
 * 必须拆成两个类。
 *
 * <p>用「按下沿」而非「按住」触发：按住不放只切一次，与 RE 的 {@code checkVariable} 标志等价。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, value = Dist.CLIENT)
public final class FRKeybinds {

    /** 不和谐之戒的开关按键，默认 X（RE 键码 45，即 LWJGL2 的 X）。 */
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
