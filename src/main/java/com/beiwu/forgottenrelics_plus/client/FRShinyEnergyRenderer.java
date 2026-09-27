package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.entity.EntityShinyEnergy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 日耀能量（{@code EntityShinyEnergy}）的占位渲染器：什么都不画。
 *
 * <p>原版与 RE 里这个实体也没有渲染器——它的全部形体就是 {@link EntityShinyEnergy} 自己
 * 每 tick 在客户端发的 8 颗 sparkle（原版 Botania 的 {@code sparkleFX} 同样是纯客户端粒子）。
 *
 * <p>之所以仍要注册一个渲染器：{@code EntityRenderDispatcher#shouldRender} 会直接
 * {@code getRenderer(entity).shouldRender(...)}，没注册就 NPE。给个空实现并把
 * {@code shadowRadius} 设成 0，避免在地面上留一圈不该有的影子。
 *
 * <p>遵守 {@code client/package-info.java} 的三条硬约束：这里连顶点都不产出。
 */
public class FRShinyEnergyRenderer extends EntityRenderer<EntityShinyEnergy> {

    /** 只为满足 {@link EntityRenderer#getTextureLocation} 的契约，实际不会被采样。 */
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ForgottenRelics.MOD_ID, "textures/entity/fr_orb.png");

    public FRShinyEnergyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityShinyEnergy entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        // 形体全部由实体 tick 里的粒子承担，这里刻意不画任何东西。
    }

    @Override
    public ResourceLocation getTextureLocation(EntityShinyEnergy entity) {
        return TEXTURE;
    }
}
