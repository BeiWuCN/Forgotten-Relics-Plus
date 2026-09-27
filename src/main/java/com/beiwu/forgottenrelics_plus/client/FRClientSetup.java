package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * 客户端初始化（模组总线）。
 *
 * <p>对应 1.12.2 原版 {@code ClientProxy.addRenderLayers()} 与
 * {@code RelicsKeybindHandler.registerKeybinds()}，这边合并成几件事：
 * <ol>
 *   <li>把恐惧之冠的几何描述交给 NeoForge 烘焙（{@code RegisterLayerDefinitions}）；</li>
 *   <li>把佩戴渲染器注册给 Curios；</li>
 *   <li>注册不和谐之戒的开关按键。</li>
 * </ol>
 *
 * <p>{@code value = Dist.CLIENT} 保证这些类只在客户端加载，服务端不会因为缺少客户端类而崩。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FRClientSetup {

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CrownCurioRenderer.CROWN_LAYER, CrownCurioRenderer::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FRKeybinds.DISCORD_RING);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(FRItems.TERROR_CROWN.get(), CrownCurioRenderer::new);
        // 键位是客户端概念，把它的显示名注入到通用物品类里，服务端则保留兜底文案。
        ItemRingOfDiscord.bindKeyHint(
                () -> FRKeybinds.DISCORD_RING.getTranslatedKeyMessage().getString());
    }

    private FRClientSetup() {
    }
}
