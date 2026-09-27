package com.beiwu.forgottenrelics_re.client;

import com.beiwu.forgottenrelics_re.ForgottenRelics;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * 客户端初始化。
 *
 * <p>对应 1.12.2 原版 {@code ClientProxy.addRenderLayers()}：那边是往 {@code RenderPlayer}
 * 上挂自定义渲染层，这边改成两件事——
 * <ol>
 *   <li>把王冠的几何描述交给 NeoForge 烘焙（{@code RegisterLayerDefinitions}）；</li>
 *   <li>把渲染器注册给 Curios（{@code CuriosRendererRegistry.register}），
 *       由 Curios 在佩戴时自动回调。</li>
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
    public static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(FRItems.TERROR_CROWN.get(), CrownCurioRenderer::new);
    }

    private FRClientSetup() {
    }
}
