package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.entity.EntityChaoticOrb;
import com.beiwu.forgottenrelicsplus.items.ItemRingOfDiscord;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FRClientSetup {

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CrownCurioRenderer.CROWN_LAYER, CrownCurioRenderer::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(FREntities.THUNDERPEAL_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.40F, 0.62F, 1.00F, 0.40F, true, true));
        event.registerEntityRenderer(FREntities.DARK_MATTER_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.16F, 0.08F, 0.34F, 0.75F, true, false));
        event.registerEntityRenderer(FREntities.CRIMSON_ORB.get(), ctx -> new FROrbRenderer<>(ctx, 0.62F, 0.06F, 0.03F, 0.45F, true, false));

        event.registerEntityRenderer(FREntities.PRIMAL_ORB.get(), ctx -> new FROrbRenderer<EntityChaoticOrb>(ctx,
                0.66F, 0.34F, 0.95F, 0.40F, true, true,
                orb -> EntityChaoticOrb.primalColorRgb(orb.getColorIndex())));
        event.registerEntityRenderer(FREntities.RAGEOUS_MISSILE.get(), ctx -> new FROrbRenderer<>(ctx, 0.95F, 0.38F, 0.10F, 0.35F, false, false));
        event.registerEntityRenderer(FREntities.SOUL_ENERGY.get(), ctx -> new FROrbRenderer<>(ctx, 0.55F, 1.00F, 0.92F, 0.35F, true, false));
        event.registerEntityRenderer(FREntities.LUNAR_FLARE.get(), ctx -> new FROrbRenderer<>(ctx, 0.86F, 0.97F, 0.86F, 0.55F, false, false));

        event.registerEntityRenderer(FREntities.SHINY_ENERGY.get(), FRShinyEnergyRenderer::new);

        event.registerEntityRenderer(FREntities.BABYLON_WEAPON.get(), FRBabylonWeaponRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FRKeybinds.DISCORD_RING);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        FRBolts.registerProvider(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(FRItems.TERROR_CROWN.get(), CrownCurioRenderer::new);

        ItemRingOfDiscord.bindKeyHint(
                () -> FRKeybinds.DISCORD_RING.getTranslatedKeyMessage().getString());
    }

    private FRClientSetup() {
    }
}
