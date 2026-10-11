package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.entity.EntityShinyEnergy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FRShinyEnergyRenderer extends EntityRenderer<EntityShinyEnergy> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    public FRShinyEnergyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityShinyEnergy entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {

    }

    @Override
    public ResourceLocation getTextureLocation(EntityShinyEnergy entity) {
        return TEXTURE;
    }
}
