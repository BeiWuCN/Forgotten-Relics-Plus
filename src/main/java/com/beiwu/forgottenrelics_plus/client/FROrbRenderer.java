package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

/**
 * 弹射物「法球」的可见渲染器。
 *
 * <h2>为什么现在要画东西了</h2>
 *
 * <p>1.7.10 的 jar 里确实没有实体贴图，那时的「法球」是靠 {@code ParticleEngine} 的粒子面片画出来的；
 * 上一版移植因此把渲染器做成空实现、只留粒子拖尾。实测下来效果是「只见拖尾、不见球」，
 * 与 1.12.2 移植版里那些 {@code RenderXxxOrb} 的观感差距很大。所以这里补一个真正的面片：
 * <b>用一张自带的柔光贴图，按相机朝向画一个公告板四边形</b>，颜色与大小按法球种类给。
 *
 * <h2>为什么这么写是安全的</h2>
 *
 * <p>{@code client/package-info.java} 的三条硬约束逐条满足：
 * <ol>
 *   <li>不碰任何 GL 状态、不手工 {@code BufferBuilder}——顶点全部来自
 *       {@link MultiBufferSource#getBuffer(RenderType)} 返回的 {@link VertexConsumer}；</li>
 *   <li>用的是原版 {@link RenderType#entityTranslucentEmissive(ResourceLocation)}，Iris 认识，
 *       且因为它是自发光类型，光照参数直接给满亮；</li>
 *   <li>走标准的 {@link EntityRenderer} + {@code EntityRenderersEvent.RegisterRenderers}。</li>
 * </ol>
 *
 * <p>与 1.12.2 那套 {@code GlStateManager} + {@code Tessellator} 的写法相比，少了「尖刺爆闪」那一层
 * （它是即时模式三角面，放进 Sodium/Iris 会出问题），但球体本身、颜色与呼吸缩放都在。
 *
 * @param <T> 法球实体类型
 */
public class FROrbRenderer<T extends Entity> extends EntityRenderer<T> {

    /** 自带的柔光贴图：中心不透明白、向边缘渐隐。 */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    /** 原版自发光实体惯用的满亮光照值（{@code LightTexture.FULL_BRIGHT}）。 */
    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    private final float red;
    private final float green;
    private final float blue;
    private final float baseScale;
    private final RenderType renderType;

    /**
     * @param red       颜色红分量 0~1
     * @param green     颜色绿分量 0~1
     * @param blue      颜色蓝分量 0~1
     * @param baseScale 基准大小（格）；实际大小会再乘上 1 + 呼吸量
     */
    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale) {
        super(context);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseScale = baseScale;
        this.renderType = RenderType.entityTranslucentEmissive(TEXTURE);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);

        // 原版那一圈 RenderXxxOrb 都用 sin(ticksExisted / 5) * 0.2 + 0.2 做「呼吸」缩放。
        float age = entity.tickCount + partialTicks;
        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        float scale = baseScale * (1.0F + bob);

        poseStack.pushPose();
        // 面朝相机：与 Thaumaturge 的 EldritchOrbRenderer 同一写法。
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(renderType);
        // 顶点缠绕与 UV 顺序照抄 Thaumaturge 的 writeBillboard。
        vertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F);
        vertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F);
        vertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F);
        vertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F);
        poseStack.popPose();
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(red, green, blue, 1.0F)
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
