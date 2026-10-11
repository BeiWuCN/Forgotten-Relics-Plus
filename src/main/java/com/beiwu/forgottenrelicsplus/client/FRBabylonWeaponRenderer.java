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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import vazkii.botania.client.core.handler.MiscellaneousModels;

public class FRBabylonWeaponRenderer extends EntityRenderer<EntityBabylonWeapon> {

    private static final ResourceLocation ORB_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");
    private static final ResourceLocation HALO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("botania", "textures/misc/babylon.png");
    private static final RenderType HALO_RENDER_TYPE = RenderType.entityTranslucentEmissive(HALO_TEXTURE, false);

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

        var motion = weapon.renderMotion();
        if (motion != null) {
            poseStack.translate(
                    motion.x(partialTicks) - net.minecraft.util.Mth.lerp(partialTicks, weapon.xOld, weapon.getX()),
                    motion.y(partialTicks) - net.minecraft.util.Mth.lerp(partialTicks, weapon.yOld, weapon.getY()),
                    motion.z(partialTicks) - net.minecraft.util.Mth.lerp(partialTicks, weapon.zOld, weapon.getZ()));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(weapon.getRotation()));

        int live = weapon.getLiveTicks();
        int delay = weapon.getDelay();

        float charge = Math.min(FULL_CHARGE_TICKS, Math.max(live, weapon.getChargeTicks()) + partialTicks);
        float chargeMul = charge / FULL_CHARGE_TICKS;

        renderWeaponModel(weapon, poseStack, buffers, packedLight);

        renderHalo(weapon, poseStack, buffers, live, delay, charge, chargeMul, partialTicks);

        poseStack.popPose();
    }

    private void renderWeaponModel(EntityBabylonWeapon weapon, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
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
                buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS)),
                null,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderHalo(EntityBabylonWeapon weapon, PoseStack poseStack, MultiBufferSource buffers,
                            int live, int delay, float charge, float chargeMul, float partialTicks) {
        float scale = chargeMul;
        if (live > delay) {
            scale -= Math.min(1.0F, (live - delay + partialTicks) * 0.2F);
        }
        if (scale <= 0.0F || chargeMul <= 0.0F) {
            return;
        }
        Random rand = new Random(weapon.getUUID().getMostSignificantBits());
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, -0.3F + rand.nextFloat() * 0.1F, 0.0F);
        scale *= 2.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                charge * 9.0F + (weapon.tickCount + partialTicks) * 0.5F + rand.nextFloat() * 360.0F));

        VertexConsumer consumer = buffers.getBuffer(HALO_RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        haloVertex(consumer, pose, -1.0F, -1.0F, 0.0F, 0.0F, chargeMul);
        haloVertex(consumer, pose, -1.0F, 1.0F, 0.0F, 1.0F, chargeMul);
        haloVertex(consumer, pose, 1.0F, 1.0F, 1.0F, 1.0F, chargeMul);
        haloVertex(consumer, pose, 1.0F, -1.0F, 1.0F, 0.0F, chargeMul);
        poseStack.popPose();
    }

    private static void haloVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                   float x, float z, float u, float v, float alpha) {
        consumer.addVertex(pose.pose(), x, 0.0F, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
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
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(ORB_TEXTURE));
        fullVertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F);
        fullVertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F);
        poseStack.popPose();
    }

    private static void fullVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose.pose(), x, y, 0.0F)
                .setColor(1.0F, 0.90F, 0.45F, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBabylonWeapon entity) {

        return InventoryMenu.BLOCK_ATLAS;
    }
}
