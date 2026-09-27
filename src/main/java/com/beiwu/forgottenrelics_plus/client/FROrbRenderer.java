package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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

/**
 * 弹射物「法球」的渲染器——**按 1.12.2 移植版（RE）的两层结构复刻**，但用现代渲染 API 写。
 *
 * <h2>复刻的是 RE 的哪两层</h2>
 *
 * <p>RE 里每个 {@code RenderXxxOrb} 都是同一个套路：
 * <ol>
 *   <li><b>外层公告板</b>：一张柔光贴片，按相机朝向、带 {@code sin(ticksExisted / 5) * 0.2 + 0.2}
 *       的呼吸缩放，染成该法球的颜色；</li>
 *   <li><b>内层尖刺爆闪</b>：12 组绕随机轴旋转的三角锥，加法混合、纯顶点色，
 *       中心白、外缘是该颜色压暗 30%，大小在前 10 tick 内线性长到 {@code (5~25)/30}。</li>
 * </ol>
 *
 * <h2>为什么不是「用原版粒子代替」</h2>
 *
 * <p>上一版曾把实体渲染器做成空实现、只留粒子拖尾，理由是「1.7.10 没有实体贴图」。
 * 但那样根本看不到球，和 RE 的观感差得远。这里改成真正把两层画出来——
 * <b>形体由渲染器负责，粒子只作拖尾</b>（拖尾仍在各实体自己的 {@code spawnTrailParticles} 里）。
 *
 * <h2>现代写法（满足 {@code client/package-info.java} 的三条硬约束）</h2>
 *
 * <p>RE 用的是 {@code GlStateManager} + {@code Tessellator}/{@code BufferBuilder} 即时模式，
 * 那在 Sodium / Iris 下会出问题。这里等价地换成：
 * <ul>
 *   <li>顶点全部来自 {@link MultiBufferSource#getBuffer(RenderType)} 的 {@link VertexConsumer}；</li>
 *   <li>姿态用 {@link PoseStack} + 原版 {@link Axis} 旋转，等价于 RE 那一串 {@code glRotatef}；</li>
 *   <li>公告板用 {@link RenderType#entityTranslucentEmissive(ResourceLocation)}（自发光、Iris 认识）；</li>
 *   <li>尖刺用 {@link RenderType#eyes(ResourceLocation)}——它正好是原版「加法混合 + 主渲染目标 +
 *       NEW_ENTITY 格式」，对应 RE 的 {@code disableTexture2D + blendFunc(SRC_ALPHA, ONE)}。
 *       <b>注意</b>：{@code RenderType.lightning()} 虽然也是加法混合，但它的输出目标是
 *       {@code WEATHER_TARGET}（天气缓冲），在实体渲染里用会画到错误的帧缓冲，所以不能用；</li>
 *   <li>随机数用 {@link RandomSource}（固定种子 187，与 RE 一致），不是 {@code Math.random()}。</li>
 * </ul>
 *
 * @param <T> 法球实体类型
 */
public class FROrbRenderer<T extends Entity> extends EntityRenderer<T> {

    /** 自带的柔光贴图：中心不透明白、向边缘渐隐。 */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    /** 原版自发光实体惯用的满亮光照值（{@code LightTexture.FULL_BRIGHT}）。 */
    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    /** 尖刺顶点统一采样贴图中心（那里最亮），这样加法混合出来是一团光而不是硬片。 */
    private static final float FLASH_U = 0.5F;
    private static final float FLASH_V = 0.5F;

    /** RE 的尖刺爆闪：固定种子 + 12 组。 */
    private static final long FLASH_SEED = 187L;
    private static final int FLASH_COUNT = 12;

    /** RE 的 {@code spikeScale = 30 / (min(ticks,10)/10)}，等价于「前 10 tick 线性放大」。 */
    private static final float FLASH_SCALE = 30.0F;
    private static final float FLASH_RAMP_TICKS = 10.0F;

    private final float red;
    private final float green;
    private final float blue;
    private final float baseScale;
    private final RenderType billboardType;
    private final RenderType flashType;

    /**
     * @param red       颜色红分量 0~1
     * @param green     颜色绿分量 0~1
     * @param blue      颜色蓝分量 0~1
     * @param baseScale 公告板基准大小（格）
     */
    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale) {
        super(context);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseScale = baseScale;
        this.billboardType = RenderType.entityTranslucentEmissive(TEXTURE);
        this.flashType = RenderType.eyes(TEXTURE);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);

        float age = entity.tickCount + partialTicks;
        renderBillboard(poseStack, buffers, age);
        renderFlash(poseStack, buffers, age);
    }

    /** 第 1 层：RE 的外层公告板。 */
    private void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, float age) {
        // 原版那一圈 RenderXxxOrb 都用 sin(ticksExisted / 5) * 0.2 + 0.2 做呼吸缩放。
        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        float scale = baseScale * (1.0F + bob);

        poseStack.pushPose();
        // 面朝相机：与 Thaumaturge 的 EldritchOrbRenderer 同一写法。
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(billboardType);
        // 顶点缠绕与 UV 顺序照抄 Thaumaturge 的 writeBillboard。
        fullVertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F, red, green, blue, 1.0F);
        fullVertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F, red, green, blue, 1.0F);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F, red, green, blue, 1.0F);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F, red, green, blue, 1.0F);
        poseStack.popPose();
    }

    /**
     * 第 2 层：RE 的尖刺爆闪。
     *
     * <p>对应 RE 那段「12 次绕 X/Y/Z 随机轴旋转 + 绕 Z 再叠一个随时间自转的角度，然后画一个
     * 三角锥」；三角锥的顶点大小用 {@code fa = (rand*20 + 5) / 30 * ramp}、
     * {@code f4 = (rand*2 + 1) / 30 * ramp}，{@code ramp = min(ticks,10)/10}。
     *
     * <p>RE 用的是 {@code GL_TRIANGLE_FAN}；本渲染类型图元是四边形，所以每个三角面用
     * 「末点重复一次」的四边形表达，渲染结果一致。
     */
    private void renderFlash(PoseStack poseStack, MultiBufferSource buffers, float age) {
        float ramp = Math.min(age, FLASH_RAMP_TICKS) / FLASH_RAMP_TICKS;
        if (ramp <= 0.0F) {
            // 第 0 tick RE 的 spikeScale 为无穷大，尖刺大小为 0，这里直接跳过。
            return;
        }
        // 外缘颜色 = 该法球颜色压暗 30%（RE 的 outerR/G/B）。
        float er = red * 0.3F;
        float eg = green * 0.3F;
        float eb = blue * 0.3F;
        float spin = age / 80.0F * 360.0F;

        RandomSource random = RandomSource.create(FLASH_SEED);
        poseStack.pushPose();
        VertexConsumer consumer = buffers.getBuffer(flashType);
        for (int i = 0; i < FLASH_COUNT; i++) {
            // 等价于 RE 的 6 次 glRotatef（第 6 次叠加自转）。
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

    /** 一个三角锥：中心白、外缘 {@code (er,eg,eb)}，三个面用四边形表达。 */
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

    /** 一个面：中心点（白）+ 两个外缘点；最后一点重复一次凑成四边形。 */
    private void spikeFace(VertexConsumer consumer, Matrix4f pose,
                           float x1, float y1, float z1, float x2, float y2, float z2,
                           float er, float eg, float eb) {
        fullVertex(consumer, pose, 0.0F, 0.0F, FLASH_U, FLASH_V, 1.0F, 1.0F, 1.0F, 1.0F);
        fullVertex(consumer, pose, x1, y1, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
        fullVertex(consumer, pose, x2, y2, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
        fullVertex(consumer, pose, x2, y2, FLASH_U, FLASH_V, er, eg, eb, 1.0F);
    }

    /** NEW_ENTITY 格式的顶点：位置 + 颜色 + UV + overlay + 光照 + 法线。z 恒为 0。 */
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
