package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
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

public final class CrownCurioRenderer implements ICurioRenderer {

    public static final ModelLayerLocation CROWN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "terror_crown"), "main");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ForgottenRelics.MOD_ID, "textures/armor/crown_prs.png");

    private static final float CROWN_THICKNESS = 3.0F;

    private ModelPart crown;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

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

        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        LivingEntity wearer = slotContext.entity();
        if (wearer == null) {
            return;
        }

        poseStack.pushPose();

        humanoid.head.translateAndRotate(poseStack);

        if (!wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            poseStack.translate(0.0F, -1.0F / 16.0F, 0.0F);
        }
        VertexConsumer buffer = buffers.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
        crown().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private ModelPart crown() {
        if (crown == null) {
            crown = Minecraft.getInstance().getEntityModels().bakeLayer(CROWN_LAYER).getChild("crown");
        }
        return crown;
    }
}
