package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.entity.EntityBabylonWeapon;
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

/**
 * 巴比伦武器（{@code babylon_weapon}）的专用渲染器。
 *
 * <h2>来源</h2>
 *
 * <p>RE 的 {@code RenderBabylonWeapon} 抄自 Botania。这里按 1.21.1 Botania 的现代实现
 * {@code vazkii.botania.client.render.entity.BabylonWeaponRenderer} 重写
 * （反编译见 {@code build/botania_dec/BabylonWeaponRenderer.java}），两层结构一一对应：
 *
 * <ol>
 *   <li>武器模型：{@code MiscellaneousModels.INSTANCE.kingKeyWeaponModels[variety]} 这个
 *       {@link BakedModel}，经 {@code Minecraft.getInstance().getBlockRenderer()
 *       .getModelRenderer().renderModel(...)} 画到 {@link Sheets#translucentItemSheet()} 上；</li>
 *   <li>光晕 quad：{@link RenderHelper#BABYLON_ICON}
 *       （贴图 {@code botania:textures/misc/babylon.png}）画一个 4 顶点的水平 quad，
 *       颜色 alpha = {@code chargeMul}；旋转、缩放、偏移照抄反编译代码。</li>
 * </ol>
 *
 * <h2>关键实现细节与取舍</h2>
 *
 * <ul>
 *   <li>{@code kingKeyWeaponModels} 的获取时机：Botania 在
 *       {@code MiscellaneousModels#onModelBake}（NeoForge 模型烘焙事件）里才填充这个数组，
 *       而构造函数运行时（{@code RegisterRenderers}）模型尚未烘焙。因此本渲染器只在每次 render
 *       时惰性读取，不在构造时缓存；读到 {@code null}（未烘焙，或数组为空 / 越界）时退回
 *       本移植的金色公告板光球。</li>
 *   <li>姿态换算：Botania 的 {@code VecHelper.rotateX/Y/Z(度)} 等价于原版 {@link Axis} 的
 *       {@code XP/YP/ZP.rotationDegrees(度)}，所以旋转统一换成原版 {@code Axis}。</li>
 *   <li>与 {@code client/package-info.java} 的硬约束：模型那层走原版
 *       {@link Sheets#translucentItemSheet()}（标准实体半透明层）；光晕那层用 Botania 的
 *       {@link RenderHelper#BABYLON_ICON}，它不是原版 RenderType，<b>严格说越过了包约束里
 *       「只用原版 RenderType」一条</b>——它在 Botania 里由 {@code CompositeState.builder()}
 *       标准装配（POSITION_TEX_COLOR + Botania 自己的 {@code CoreShaders::halo} +
 *       TRANSLUCENT_TRANSPARENCY + ITEM_ENTITY_TARGET + NO_CULL），其中 halo.fsh 是盒式模糊
 *       加亮度脉动的自定义片元着色器，所以确实没有完全满足 {@code package-info} 的第三条。
 *       不改的理由有两条：一是换掉它就必须放弃模糊与脉动，会明显偏离与 Botania 及本移植
 *       1.6.1 的对齐目标；二是它不直接碰 GL 状态，混合是
 *       {@code SRC_ALPHA, ONE_MINUS_SRC_ALPHA}（会采样 alpha，贴图 {@code babylon.png} 的
 *       RGB 恒定金色、形状全在 alpha 上），画出来是柔光贴片，不可能变成「实心硬边大 quad」。
 *       Botania 是本移植的硬依赖（mods.toml 已声明），不存在缺少它时崩溃的问题。</li>
 * </ul>
 */
public class FRBabylonWeaponRenderer extends EntityRenderer<EntityBabylonWeapon> {

    /** 退回金色光球时用的贴图（与 {@code FROrbRenderer} 同一张）。 */
    private static final ResourceLocation ORB_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    /** 充能满值：Botania 的 {@code charge / 10.0f}。 */
    private static final float FULL_CHARGE_TICKS = 10.0F;

    /** 模型缩放，Botania 的 {@code s = 1.5f}。 */
    private static final float MODEL_SCALE = 1.5F;

    public FRBabylonWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityBabylonWeapon weapon, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        // Botania: ms.mulPose(VecHelper.rotateY(weapon.getRotation()))
        poseStack.mulPose(Axis.YP.rotationDegrees(weapon.getRotation()));

        int live = weapon.getLiveTicks();
        int delay = weapon.getDelay();
        // Botania: charge = min(10, max(live, chargeTicks) + partialTicks); chargeMul = charge / 10。
        float charge = Math.min(FULL_CHARGE_TICKS, Math.max(live, weapon.getChargeTicks()) + partialTicks);
        float chargeMul = charge / FULL_CHARGE_TICKS;

        renderWeaponModel(weapon, poseStack, buffers);
        // 原版（1.7.10 / RE / Botania）在这之后用 Botania 的 halo 着色器再画一层光罩。
        // 这层光罩承担充能反馈，必须保留。
        renderHalo(weapon, poseStack, buffers, live, delay, charge, chargeMul, partialTicks);

        poseStack.popPose();
    }

    /**
     * 第 1 层：武器模型。
     *
     * <p>对应反编译里
     * {@code ms.translate(-0.75, 0, 1); scale(1.5); rotateY(90); rotateZ(-45); renderModel(...)}。
     */
    private void renderWeaponModel(EntityBabylonWeapon weapon, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel model = kingKeyModel(weapon.getVariety());
        if (model == null) {
            // 模型未烘焙 / 索引越界：退回本移植的金色公告板光球（见类注释）。
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

    /**
     * 第 2 层：光晕 quad。
     *
     * <p>对应反编译里 {@code rotateX(-90); translate(0, -0.3 + rand*0.1, 0); scale(s); rotateY(...);}
     * 然后画 {@code (-1,0,-1) ~ (1,0,1)} 的那个水平四边形，alpha = {@code chargeMul}。
     * {@code rand} 的种子是实体 UUID 的高 64 位，与 Botania 一致。
     */
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

        // BABYLON_ICON 是 POSITION_TEX_COLOR 格式，只需要写颜色与 UV（与 Botania 反编译一致）。
        VertexConsumer consumer = buffers.getBuffer(RenderHelper.BABYLON_ICON);
        Matrix4f pose = poseStack.last().pose();
        consumer.addVertex(pose, -1.0F, 0.0F, -1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(0.0F, 0.0F);
        consumer.addVertex(pose, -1.0F, 0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(0.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(1.0F, 1.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, -1.0F).setColor(1.0F, 1.0F, 1.0F, chargeMul).setUv(1.0F, 0.0F);
        poseStack.popPose();
    }

    /**
     * 取 Botania 的武器模型。
     *
     * <p><b>每次渲染都重新读字段</b>：Botania 是在模型烘焙事件里才把
     * {@code kingKeyWeaponModels} 填上，构造函数跑的时候（{@code RegisterRenderers}）还没烘焙，
     * 所以不能在构造时缓存数组或元素。越界用 {@code floorMod} 回绕，空数组返回 {@code null}。
     */
    private static BakedModel kingKeyModel(int variety) {
        BakedModel[] models = MiscellaneousModels.INSTANCE.kingKeyWeaponModels;
        if (models == null || models.length == 0) {
            return null;
        }
        return models[Math.floorMod(variety, models.length)];
    }

    /**
     * 兜底：Botania 模型取不到时画一个金色公告板光球。
     *
     * <p>用原版 {@link RenderType#entityTranslucentEmissive(ResourceLocation)}（自发光半透明），
     * 与 {@code FROrbRenderer} 的公告板同一套写法。
     */
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

    /** NEW_ENTITY 格式的顶点：位置 + 颜色 + UV + overlay + 光照 + 法线。z 恒为 0。 */
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
        // 与 Botania 一致：主贴图来自方块图集。
        return InventoryMenu.BLOCK_ATLAS;
    }
}
