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
import org.joml.Quaternionf;

public class FROrbRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    private static final float MAX_BILLBOARD_SCALE = 1.1F;

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    private static final float FLASH_U = 0.5F;
    private static final float FLASH_V = 0.5F;

    private static final long FLASH_SEED = 187L;
    private static final int FLASH_COUNT = 12;

    private static final float FLASH_SCALE = 30.0F;
    private static final float FLASH_RAMP_TICKS = 10.0F;
    private static final FlashParameters[] FLASH_PARAMETERS = createFlashParameters();

    private final float red;
    private final float green;
    private final float blue;
    private final float baseScale;
    private final boolean renderBillboard;
    private final RenderType billboardType;
    private final RenderType flashType;

    private final ToIntFunction<T> colorSource;

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale) {
        this(context, red, green, blue, baseScale, true, null);
    }

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard) {
        this(context, red, green, blue, baseScale, renderBillboard, null);
    }

    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard, ToIntFunction<T> colorSource) {
        super(context);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseScale = baseScale;
        this.renderBillboard = renderBillboard;

        this.billboardType = RenderType.entityTranslucentEmissive(TEXTURE, false);
        this.flashType = this.billboardType;
        this.colorSource = colorSource;
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        if (entity instanceof FRHomingProjectile projectile && projectile.renderMotion() != null) {
            var motion = projectile.renderMotion();
            poseStack.translate(
                    motion.x(partialTicks) - Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                    motion.y(partialTicks) - Mth.lerp(partialTicks, entity.yOld, entity.getY()),
                    motion.z(partialTicks) - Mth.lerp(partialTicks, entity.zOld, entity.getZ()));
        }
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
        renderFlash(poseStack, buffers, age, r, g, b, fade);
        poseStack.popPose();
    }

    private record FlashParameters(Quaternionf rotation, float height, float width) {}

    private static FlashParameters[] createFlashParameters() {
        RandomSource random = RandomSource.create(FLASH_SEED);
        FlashParameters[] parameters = new FlashParameters[FLASH_COUNT];
        for (int i = 0; i < parameters.length; i++) {
            Quaternionf rotation = new Quaternionf();
            for (int cycle = 0; cycle < 2; cycle++) {
                rotation.rotateX(random.nextFloat() * (float) (Math.PI * 2));
                rotation.rotateY(random.nextFloat() * (float) (Math.PI * 2));
                rotation.rotateZ(random.nextFloat() * (float) (Math.PI * 2));
            }
            float height = (random.nextFloat() * 20.0F + 5.0F) / FLASH_SCALE;
            float width = (random.nextFloat() * 2.0F + 1.0F) / FLASH_SCALE;
            parameters[i] = new FlashParameters(rotation, height, width);
        }
        return parameters;
    }

    private void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b,
                                 float fade) {

        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        float scale = Math.min(this.baseScale * (1.0F + bob), MAX_BILLBOARD_SCALE) * fade;
        if (scale <= 0.0F) return;

        poseStack.pushPose();

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffers.getBuffer(this.billboardType);

        fullVertex(consumer, pose, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, r, g, b, fade, 0, 0, 1);
        fullVertex(consumer, pose, -0.5F, 0.5F, 0.0F, 1.0F, 0.0F, r, g, b, fade, 0, 0, 1);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, r, g, b, fade, 0, 0, 1);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 0.0F, 1.0F, r, g, b, fade, 0, 0, 1);
        poseStack.popPose();
    }

    private void renderFlash(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b, float fade) {
        float ramp = Math.min(age, FLASH_RAMP_TICKS) / FLASH_RAMP_TICKS;
        if (ramp <= 0.0F || fade <= 0.0F) {

            return;
        }

        float er = r * 0.3F;
        float eg = g * 0.3F;
        float eb = b * 0.3F;
        float spin = age / 80.0F * 360.0F;

        poseStack.pushPose();
        VertexConsumer consumer = buffers.getBuffer(this.flashType);
        Quaternionf spinRotation = Axis.ZP.rotationDegrees(spin);
        for (FlashParameters spike : FLASH_PARAMETERS) {
            poseStack.mulPose(spike.rotation());
            poseStack.mulPose(spinRotation);
            float fa = spike.height() * ramp;
            float f4 = spike.width() * ramp;
            addSpike(consumer, poseStack.last(), fa, f4, er, eg, eb, fade);
        }
        poseStack.popPose();
    }

    private void addSpike(VertexConsumer consumer, PoseStack.Pose pose, float fa, float f4,
                          float er, float eg, float eb, float fade) {
        float ax = -0.866F * f4;
        float az = -0.5F * f4;
        float bx = 0.866F * f4;
        float bz = -0.5F * f4;
        float cz = f4;
        spikeFace(consumer, pose, ax, fa, az, bx, fa, bz, er, eg, eb, fade);
        spikeFace(consumer, pose, bx, fa, bz, 0.0F, fa, cz, er, eg, eb, fade);
        spikeFace(consumer, pose, 0.0F, fa, cz, ax, fa, az, er, eg, eb, fade);
    }

    private void spikeFace(VertexConsumer consumer, PoseStack.Pose pose,
                           float x1, float y1, float z1, float x2, float y2, float z2,
                           float er, float eg, float eb, float fade) {
        float nx = y1 * z2 - z1 * y2;
        float ny = z1 * x2 - x1 * z2;
        float nz = x1 * y2 - y1 * x2;
        float inverseLength = Mth.invSqrt(nx * nx + ny * ny + nz * nz);
        nx *= inverseLength;
        ny *= inverseLength;
        nz *= inverseLength;
        fullVertex(consumer, pose, 0, 0, 0, FLASH_U, FLASH_V, 1, 1, 1, fade,
                nx, ny, nz);
        fullVertex(consumer, pose, x1, y1, z1, FLASH_U, FLASH_V, er, eg, eb, fade * 0.12F,
                nx, ny, nz);
        fullVertex(consumer, pose, x2, y2, z2, FLASH_U, FLASH_V, er, eg, eb, fade * 0.12F,
                nx, ny, nz);
        fullVertex(consumer, pose, x2, y2, z2, FLASH_U, FLASH_V, er, eg, eb, fade * 0.12F,
                nx, ny, nz);
    }

    private static void fullVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                                   float u, float v, float r, float g, float b, float a,
                                   float nx, float ny, float nz) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
