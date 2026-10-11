package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.entity.FRHomingProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

public class FROrbRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    private static final ResourceLocation ADD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb_add.png");

    private static final float MAX_BILLBOARD_SCALE = 1.1F;

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    private static final float FLASH_U = 0.5F;
    private static final float FLASH_V = 0.5F;

    private static final long FLASH_SEED = 187L;
    private static final int FLASH_COUNT = 12;

    private static final float FLASH_SCALE = 30.0F;
    private static final float FLASH_RAMP_TICKS = 10.0F;

    private final float red;
    private final float green;
    private final float blue;
    private final float baseScale;
    private final boolean renderBillboard;
    private final RenderType billboardType;
    private final RenderType flashType;

    private final ToIntFunction<T> colorSource;

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale) {
        this(context, red, green, blue, baseScale, true, false, null);
    }

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard, boolean additiveBillboard) {
        this(context, red, green, blue, baseScale, renderBillboard, additiveBillboard, null);
    }

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard, boolean additiveBillboard, ToIntFunction<T> colorSource) {
        super(context);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseScale = baseScale;
        this.renderBillboard = renderBillboard;

        this.billboardType = additiveBillboard
                ? RenderType.eyes(ADD_TEXTURE)
                : RenderType.entityTranslucentEmissive(TEXTURE);
        this.flashType = RenderType.eyes(ADD_TEXTURE);
        this.colorSource = colorSource;
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);

        float age = entity.tickCount + partialTicks;

        float r = this.red;
        float g = this.green;
        float b = this.blue;
        if (this.colorSource != null) {
            int rgb = this.colorSource.applyAsInt(entity);
            r = ((rgb >> 16) & 0xFF) / 255.0F;
            g = ((rgb >> 8) & 0xFF) / 255.0F;
            b = (rgb & 0xFF) / 255.0F;
        }

        float fade = entity instanceof FRHomingProjectile projectile ? projectile.renderScale() : 1.0F;

        if (this.renderBillboard) {
            renderBillboard(poseStack, buffers, age, r, g, b, fade);
        }
        renderFlash(poseStack, buffers, age, r, g, b);
    }

    private void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b,
                                 float fade) {

        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        float scale = Math.min(this.baseScale * (1.0F + bob), MAX_BILLBOARD_SCALE) * fade;

        poseStack.pushPose();

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(this.billboardType);

        fullVertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F, r, g, b, 1.0F);
        poseStack.popPose();
    }

    private void renderFlash(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b) {
        float ramp = Math.min(age, FLASH_RAMP_TICKS) / FLASH_RAMP_TICKS;
        if (ramp <= 0.0F) {

            return;
        }

        float er = r * 0.3F;
        float eg = g * 0.3F;
        float eb = b * 0.3F;
        float spin = age / 80.0F * 360.0F;

        RandomSource random = RandomSource.create(FLASH_SEED);
        poseStack.pushPose();
        VertexConsumer consumer = buffers.getBuffer(this.flashType);
        for (int i = 0; i < FLASH_COUNT; i++) {

            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + spin));

            float fa = (random.nextFloat() * 20.0F + 5.0F) / FLASH_SCALE * ramp;
            float f4 = (random.nextFloat() * 2.0F + 1.0F) / FLASH_SCALE * ramp;
            addSpike(consumer, poseStack.last().pose(), fa, f4, er, eg, eb);
        }
        poseStack.popPose();
    }

    private void addSpike(VertexConsumer consumer, Matrix4f pose, float fa, float f4,
                          float er, float eg, float eb) {
        float ax = -0.866F * f4;
        float az = -0.5F * f4;
        float bx = 0.866F * f4;
        float bz = -0.5F * f4;
        float cz = f4;
        spikeFace(consumer, pose, ax, fa, az, bx, fa, bz, er, eg, eb);
        spikeFace(consumer, pose, bx, fa, bz, 0.0F, fa, cz, er, eg, eb);
        spikeFace(consumer, pose, 0.0F, fa, cz, ax, fa, az, er, eg, eb);
    }

    private void spikeFace(VertexConsumer consumer, Matrix4f pose,
                           float x1, float y1, float z1, float x2, float y2, float z2,
                           float er, float eg, float eb) {
        fullVertex(consumer, pose, 0.0F, 0.0F, FLASH_U, FLASH_V, 1.0F, 1.0F, 1.0F, 1.0F);
        fullVertex(consumer, pose, x1, y1, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
        fullVertex(consumer, pose, x2, y2, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
        fullVertex(consumer, pose, x2, y2, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
    }

    private static void fullVertex(VertexConsumer consumer, Matrix4f pose, float x, float y,
                                   float u, float v, float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
