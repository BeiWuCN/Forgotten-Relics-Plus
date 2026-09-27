package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * 恐惧之冠的佩戴渲染。
 *
 * <p>1.12.2 的做法是往 {@code RenderPlayer} 上挂一层 {@code LayerCrown}，里面手工
 * {@code bindTexture} + 复制头部角度 + 逐个 ModelRenderer 画；那条路子依赖
 * {@code ModelBiped} 与固定管线，1.21.1 已经没有了。
 *
 * <p>这里换成现代写法：
 * <ul>
 *   <li>用 {@link LayerDefinition} 描述一个 8×3×8 的「冠」立方体（等价于原版 ModelCrown 里
 *       那个 {@code addBox(-4, y, -4, 8, 3, 8)}），由 NeoForge 的
 *       {@code EntityRenderersEvent.RegisterLayerDefinitions} 在客户端初始化时烘焙成
 *       {@link ModelPart}；</li>
 *   <li>用 {@link ICurioRenderer} 注册到 Curios，戴在饰品栏时由 Curios 自动回调；</li>
 *   <li>用 {@link HumanoidModel#head}{@code .translateAndRotate} 继承头部姿态，
 *       不再手工拷贝十个字段。</li>
 * </ul>
 *
 * <p>与 1.12.2 一致的两处细节：戴着头盔时整体上抬 1 像素，且帽子层的显隐跟随原版模型。
 */
public final class CrownCurioRenderer implements ICurioRenderer {

    /** 模型层注册键，见 {@code FRClientSetup}。 */
    public static final ModelLayerLocation CROWN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "terror_crown"), "main");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ForgottenRelics.MOD_ID, "textures/armor/crown_prs.png");

    /** 原版 ModelCrown 的盒子：宽 8、高 3、深 8，绕头部原点摆一圈。 */
    private static final float CROWN_THICKNESS = 3.0F;

    private ModelPart crown;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // y = -8 是头顶（原版头盒为 -8..0），冠体从 -11 抬到 -8，正好骑在头上。
        root.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F - CROWN_THICKNESS, -4.0F, 8, 3, 8),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffers,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        // 只有人形模型才有头可骑；其余情况直接跳过。
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        LivingEntity wearer = slotContext.entity();
        if (wearer == null) {
            return;
        }

        poseStack.pushPose();
        // 继承头盔姿态：位置与朝向都跟着头走。
        humanoid.head.translateAndRotate(poseStack);
        // 与 1.12.2 一致：头顶已经有别的头饰时上抬 1 像素，避免穿插。
        if (!wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            poseStack.translate(0.0F, -1.0F / 16.0F, 0.0F);
        }
        VertexConsumer buffer = buffers.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
        crown().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /**
     * 懒加载烘焙模型。
     *
     * <p>烘焙一次即可复用，不必每帧重建——这正是相对 1.12.2「每帧 new 一个 ModelCrown」的优化点。
     */
    private ModelPart crown() {
        if (crown == null) {
            crown = Minecraft.getInstance().getEntityModels().bakeLayer(CROWN_LAYER).getChild("crown");
        }
        return crown;
    }
}
