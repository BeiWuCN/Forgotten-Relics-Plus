package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
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
 * <h2>逐实体的差异（本次 1.6.1 对齐）</h2>
 *
 * <p>对照 RE 的各个 {@code RenderXxx} 逐个实体校准，需要三个开关：
 * <ul>
 *   <li><b>要不要画公告板</b>：RE 的 {@code RenderLunarFlare} 与 {@code RenderRageousMissile}
 *       <b>只有尖刺层</b>，没有公告板；其余渲染器两层都有；</li>
 *   <li><b>公告板是不是加法混合</b>：RE 的 {@code RenderThunderpealOrb} 与 {@code RenderPrimalOrb}
 *       公告板用 {@code blendFunc(SRC_ALPHA, ONE)}（加法），而 {@code RenderCrimsonOrb} /
 *       {@code RenderDarkMatterOrb} 用 {@code SRC_ALPHA, ONE_MINUS_SRC_ALPHA}（普通透明）；</li>
 *   <li><b>颜色是不是按实体取</b>：RE 的 {@code RenderPrimalOrb} 用 {@code entity.getColorIndex()}
 *       从 6 色表取色，其余法球的颜色写死在各自的渲染器里。</li>
 * </ul>
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
 *   <li>公告板：普通透明用 {@link RenderType#entityTranslucentEmissive(ResourceLocation)}
 *       （自发光、Iris 认识）；加法时用 {@link RenderType#eyes(ResourceLocation)}；</li>
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

    /**
     * 普通透明层用的柔光贴图：<b>RGB 全白、只有 alpha 呈径向衰减</b>。
     *
     * <p>只适合 {@code SRC_ALPHA, ONE_MINUS_SRC_ALPHA} 这类会采样 alpha 的混合。
     */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    /**
     * 加法层用的柔光贴图：<b>RGB 就是径向衰减（中心 236、边缘 0）、alpha 恒为 255</b>。
     * 由 {@code Tools/make_orb_add_texture.js} 从 {@link #TEXTURE} 生成。
     *
     * <p>为什么加法层不能用 {@link #TEXTURE}：原版 {@link RenderType#eyes(ResourceLocation)} 的混合是
     * {@code ADDITIVE_TRANSPARENCY}，其实现是 {@code RenderSystem.blendFunc(ONE, ONE)}——
     * <b>alpha 完全不参与混合</b>（顶点 alpha 也一样被忽略），shader 只把「贴图 RGB × 顶点色」直接加到帧缓冲。
     * 于是「RGB 全白、只有 alpha 有渐变」的贴图在加法层里会被当成一整块实心白 quad 加起来：
     * 边缘没有渐变、四边形覆盖到哪里就把那里刷亮到哪里，贴近相机时就是玩家反馈的那块「巨大的硬边半透明面片」。
     * RE 的 {@code RenderThunderpealOrb} 用的是 {@code blendFunc(SRC_ALPHA, ONE)}（会乘 alpha），
     * 1.21.1 没有「贴图 + SRC_ALPHA,ONE + 主渲染目标」的现成 RenderType，所以这里改为把衰减放进 RGB，
     * 用它配合 {@code eyes} 的 {@code ONE,ONE} 得到等价的柔光加法效果。
     */
    private static final ResourceLocation ADD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb_add.png");

    /**
     * 公告板尺寸上限（格）。
     *
     * <p>纯属防御：目前最大的实体是暗物质球 {@code 0.75 * (1 + bob)} ≈ 1.05，本上限不会改变现有观感，
     * 只是保证以后有人把 {@code baseScale} 调飞时不会再一次出现「一块 quad 糊满屏幕」。
     */
    private static final float MAX_BILLBOARD_SCALE = 1.1F;

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
    private final boolean renderBillboard;
    private final RenderType billboardType;
    private final RenderType flashType;

    /**
     * 颜色来源：返回打包成 {@code 0xRRGGBB} 的颜色。{@code null} 表示用构造时传入的固定色。
     *
     * <p>只有原初球需要它——对应 RE 的 {@code entity.getColorIndex()}。
     */
    private final ToIntFunction<T> colorSource;

    /**
     * 固定颜色、画公告板、公告板普通透明（沿用旧调用点行为的便捷构造）。
     *
     * @param red       颜色红分量 0~1
     * @param green     颜色绿分量 0~1
     * @param blue      颜色蓝分量 0~1
     * @param baseScale 公告板基准大小（格）
     */
    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale) {
        this(context, red, green, blue, baseScale, true, false, null);
    }

    /**
     * 固定颜色的便捷构造。
     *
     * @param renderBillboard  是否绘制外层公告板（RE 的 lunar_flare / rageous_missile 传 {@code false}）
     * @param additiveBillboard 公告板是否用加法混合（RE 的 thunderpeal / primal 传 {@code true}）
     */
    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard, boolean additiveBillboard) {
        this(context, red, green, blue, baseScale, renderBillboard, additiveBillboard, null);
    }

    /**
     * 完整构造。
     *
     * @param renderBillboard   是否绘制外层公告板
     * @param additiveBillboard 公告板是否用加法混合；{@code true} 时公告板与尖刺共用
     *                          {@link RenderType#eyes(ResourceLocation)}，{@code false} 时用
     *                          {@link RenderType#entityTranslucentEmissive(ResourceLocation)}
     * @param colorSource       颜色来源，{@code null} 表示用固定色
     */
    public FROrbRenderer(EntityRendererProvider.Context context, float red, float green, float blue, float baseScale,
                         boolean renderBillboard, boolean additiveBillboard, ToIntFunction<T> colorSource) {
        super(context);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseScale = baseScale;
        this.renderBillboard = renderBillboard;
        // RE 的加法公告板是 blendFunc(SRC_ALPHA, ONE) + 主渲染目标；原版最接近的是 eyes()（加法 + 主目标），
        // 但它内部是 blendFunc(ONE, ONE)、不采样 alpha，所以加法层必须配「衰减在 RGB 里」的 ADD_TEXTURE，
        // 不能用只有 alpha 渐变的 TEXTURE（否则是一块实心 quad，见 ADD_TEXTURE 的注释）。
        // 普通透明对应 RE 的 blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)，用自发光半透明实体层，配 alpha 渐变的 TEXTURE。
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
        // 颜色：原初球按同步过来的要素索引取色（RE 的 entity.getColorIndex()），其余实体用构造时的固定色。
        float r = this.red;
        float g = this.green;
        float b = this.blue;
        if (this.colorSource != null) {
            int rgb = this.colorSource.applyAsInt(entity);
            r = ((rgb >> 16) & 0xFF) / 255.0F;
            g = ((rgb >> 8) & 0xFF) / 255.0F;
            b = (rgb & 0xFF) / 255.0F;
        }

        if (this.renderBillboard) {
            renderBillboard(poseStack, buffers, age, r, g, b);
        }
        renderFlash(poseStack, buffers, age, r, g, b);
    }

    /** 第 1 层：RE 的外层公告板（lunar_flare / rageous_missile 不画）。 */
    private void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b) {
        // 原版那一圈 RenderXxxOrb 都用 sin(ticksExisted / 5) * 0.2 + 0.2 做呼吸缩放。
        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        float scale = Math.min(this.baseScale * (1.0F + bob), MAX_BILLBOARD_SCALE);

        poseStack.pushPose();
        // 面朝相机（标准公告板写法，纯原版 API，不依赖任何前置模组）。
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, scale, scale);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(this.billboardType);
        // 顶点缠绕与 UV 顺序照抄 Thaumaturge 的 writeBillboard。
        fullVertex(consumer, pose, -0.5F, -0.5F, 1.0F, 1.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, -0.5F, 0.5F, 1.0F, 0.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, 0.5F, 0.5F, 0.0F, 0.0F, r, g, b, 1.0F);
        fullVertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F, r, g, b, 1.0F);
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
    private void renderFlash(PoseStack poseStack, MultiBufferSource buffers, float age, float r, float g, float b) {
        float ramp = Math.min(age, FLASH_RAMP_TICKS) / FLASH_RAMP_TICKS;
        if (ramp <= 0.0F) {
            // 第 0 tick RE 的 spikeScale 为无穷大，尖刺大小为 0，这里直接跳过。
            return;
        }
        // 外缘颜色 = 该法球颜色压暗 30%（RE 的 outerR/G/B）。
        float er = r * 0.3F;
        float eg = g * 0.3F;
        float eb = b * 0.3F;
        float spin = age / 80.0F * 360.0F;

        RandomSource random = RandomSource.create(FLASH_SEED);
        poseStack.pushPose();
        VertexConsumer consumer = buffers.getBuffer(this.flashType);
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
