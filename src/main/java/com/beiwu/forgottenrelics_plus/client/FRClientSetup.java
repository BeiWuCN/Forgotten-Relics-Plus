package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.entity.EntityChaoticOrb;
import com.beiwu.forgottenrelics_plus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
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

    /**
     * 实体渲染器注册。
     *
     * <p>注意这里用的是 {@code RegisterRenderers}，不是上面那个 {@code RegisterLayerDefinitions}——
     * 两个事件的职责不同，放错事件编译期就会报「找不到符号」。
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 法球：用可见的公告板渲染器（自带柔光贴图 + 呼吸缩放），颜色按种类给。
        // 见 FROrbRenderer 的类注释——它满足 client/package-info.java 的三条硬约束。
        //
        // 构造参数末两位 = (是否画公告板, 公告板是否加法混合)，逐条对照 RE：
        //   thunderpeal：两层都有，公告板 blendFunc(SRC_ALPHA, ONE) → 加法；
        //               尺寸照 RE 的 RenderThunderpealOrb 取 0.4（1.6.2 前误用 0.45）。
        //   darkmatter / crimson：两层都有，公告板 blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA) → 普通透明；
        //   primal：两层都有，公告板也是 blendFunc(SRC_ALPHA, ONE) → 加法；颜色改由同步索引决定；
        //   rageous / lunar：RE 只有尖刺层，不画公告板；
        //   soul：RE 没有对应渲染器（Thaumaturge 侧替代），沿用上一版行为（画公告板、普通透明）。
        event.registerEntityRenderer(FREntities.THUNDERPEAL_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.40F, 0.62F, 1.00F, 0.40F, true, true));
        event.registerEntityRenderer(FREntities.DARK_MATTER_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.16F, 0.08F, 0.34F, 0.75F, true, false));
        event.registerEntityRenderer(FREntities.CRIMSON_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.62F, 0.06F, 0.03F, 0.45F, true, false));
        // 原初球：颜色按同步过来的要素索引从 EntityChaoticOrb.PRIMAL_COLORS 取（RE 的 getColorIndex()）。
        event.registerEntityRenderer(FREntities.PRIMAL_ORB.get(), ctx -> new FROrbRenderer<EntityChaoticOrb>(ctx,
                0.66F, 0.34F, 0.95F, 0.40F, true, true,
                orb -> EntityChaoticOrb.primalColorRgb(orb.getColorIndex())));
        event.registerEntityRenderer(FREntities.RAGEOUS_MISSILE.get(), ctx -> new FROrbRenderer<>(ctx, 0.95F, 0.38F, 0.10F, 0.35F, false, false));
        event.registerEntityRenderer(FREntities.SOUL_ENERGY.get(), ctx -> new FROrbRenderer<>(ctx, 0.55F, 1.00F, 0.92F, 0.35F, true, false));
        event.registerEntityRenderer(FREntities.LUNAR_FLARE.get(), ctx -> new FROrbRenderer<>(ctx, 0.86F, 0.97F, 0.86F, 0.55F, false, false));
        // 日耀能量：不画实体本身，形体全在实体 tick 里发的 sparkle 粒子上（见 FRShinyEnergyRenderer）。
        event.registerEntityRenderer(FREntities.SHINY_ENERGY.get(), FRShinyEnergyRenderer::new);
        // 巴比伦武器：改用 Botania 的 King Key 武器模型 + 光晕 quad（专用渲染器）。
        event.registerEntityRenderer(FREntities.BABYLON_WEAPON.get(), FRBabylonWeaponRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FRKeybinds.DISCORD_RING);
    }

    /**
     * 闪电弧的粒子 provider。
     *
     * <p>1.7.10 / RE 是「自定义网络包 → 客户端收包后调 Thaumcraft 画电弧」；
     * 这里换成「原版粒子包 → 客户端 provider 调 Botania 的 {@code BoltRenderer}」，
     * 所以只需要在这一处把 provider 挂上，见 {@link FRBolts}。
     */
    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        FRBolts.registerProvider(event);
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
