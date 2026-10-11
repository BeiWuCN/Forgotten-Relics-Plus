package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.entity.EntityBabylonWeapon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;

public class FRBabylonWeaponRenderer extends EntityRenderer<EntityBabylonWeapon> {

    private static final ResourceLocation ORB_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    private static final float FULL_CHARGE_TICKS = 10.0F;

    private static final float MODEL_SCALE = 1.5F;

    public FRBabylonWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityBabylonWeapon weapon, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(weapon.getRotation()));

        int live = weapon.getLiveTicks();
        int delay = weapon.getDelay();

        float charge = Math.min(FULL_CHARGE_TICKS, Math.max(live, weapon.getChargeTicks()) + partialTicks);
        float chargeMul = charge / FULL_CHARGE_TICKS;

        renderWeaponModel(weapon, poseStack, buffers);

        renderHalo(weapon, poseStack, buffers, live, delay, charge, chargeMul, partialTicks);

        poseStack.popPose();
    }

    private void renderWeaponModel(EntityBabylonWeapon weapon, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel model = kingKeyModel(weapon.getVariety());
        if (model == null) {

            renderFallbackOrb(poseStack, buffers);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.75D, 0.0D, 1.0D);
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffers.getBuffer(Sheets.translucentItemSheet()),
                null,
                model,
                1.0F, 1.0F, 1.0F,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderHalo(EntityBabylonWeapon weapon, PoseStack poseStack, MultiBufferSource buffers,
                            int live, int delay, float charge, float chargeMul, float partialTicks) {
        Random rand = new Random(weapon.getUUID().getMostSignificantBits());
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, -0.3F + rand.nextFloat() * 0.1F, 0.0F);
        float scale = chargeMul;
        if (live > delay) {
            scale -= Math.min(1.0F, (live - delay + partialTicks) * 0.2F);
        }
        scale *= 2.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                charge * 9.0F + (weapon.tickCount + partialTicks) * 0.5F + rand.nextFloat() * 360.0F));

        VertexConsumer consumer = buffers.getBuffer(RenderHelper.BABYLON_ICON);
        Matrix4f pose = poseStack.last().pose();
        consumer.addVertex(pose, -1.0F, 0.0F, -1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(0.0F, 0.0F);
        consumer.addVertex(pose, -1.0F, 0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(0.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(1.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, -1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(1.0F, 0.0F);
        poseStack.popPose();
    }

    private static BakedModel kingKeyModel(int variety) {
        BakedModel[] models = MiscellaneousModels.INSTANCE.kingKeyWeaponModels;
        if (models == null || models.length == 0) {
            return null;
        }
        return models[Math.floorMod(variety, models.length)];
    }

    private void renderFallbackOrb(PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.55F, 0.55F, 0.55F);
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(ORB_TEXTURE));
        fullVertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F);
        fullVertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F);
        poseStack.popPose();
    }

    private static void fullVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(1.0F, 0.90F, 0.45F, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBabylonWeapon entity) {

        return InventoryMenu.BLOCK_ATLAS;
    }
}
