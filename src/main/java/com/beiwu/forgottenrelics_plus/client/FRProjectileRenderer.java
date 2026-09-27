package com.beiwu.forgottenrelics_plus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 弹射物的占位渲染器：<b>不绘制任何几何体</b>。
 *
 * <p>这不是偷懒。1.7.10 的 jar 里没有实体贴图，那些球体的渲染器用的是
 * {@code ParticleEngine.particleTexture}——也就是「拿粒子图集画一个二维面片」。
 * 本项目沿用了同一思路：实体只负责逻辑，可见的形体与拖尾全部由原版粒子承担
 * （见 {@code FRHomingProjectile#spawnTrailParticles}）。
 *
 * <p>这样做的额外好处正合 {@code package-info.java} 的约束：完全不碰自定义 {@code RenderType}
 * 与顶点构建，Sodium / Iris 下不存在兼容问题。
 *
 * <p>{@link #getTextureLocation} 不会被真正使用，但 {@code EntityRenderer} 要求实现它。
 */
public final class FRProjectileRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final ResourceLocation UNUSED = ResourceLocation.withDefaultNamespace("textures/particle/particles.png");

    public FRProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        // 空实现：形体由粒子表现。
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return UNUSED;
    }
}
