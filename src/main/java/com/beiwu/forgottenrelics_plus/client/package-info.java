/**
 * 客户端渲染。
 *
 * <h2>硬性约束：必须与 Sodium / Iris 共存</h2>
 *
 * <p>Sodium 重写了区块与地形渲染的批次提交，Iris 在它之上包了一层光影管线。自定义渲染是这两者
 * 最容易出问题的地方——不是编译期能发现的错，而是「装了光影就变黑/闪烁/错位/崩溃」。因此本包
 * 内的所有渲染代码必须遵守以下三条，新增渲染器时逐条自查：
 *
 * <ol>
 *   <li><b>绝不直接操作 GL 状态。</b>禁止 {@code GlStateManager}、{@code RenderSystem.setShader}
 *       /{@code setShaderTexture}、{@code RenderSystem.enableBlend} 之类调用，也禁止
 *       {@code Tesselator}/{@code BufferBuilder} 手工构建顶点。这些在 Sodium 下会与它的批次
 *       管理冲突，在 Iris 下会绕过材质与光照解析。</li>
 *   <li><b>只用原版渲染路径。</b>顶点一律通过 {@link net.minecraft.client.renderer.MultiBufferSource}
 *       拿 {@link com.mojang.blaze3d.vertex.VertexConsumer}，类型用原版 {@code RenderType}
 *       （如 {@code entityCutout}/{@code entityTranslucentEmissive}/{@code armorCutoutNoCull}），
 *       这些是 Iris 认识的。自定义 {@code RenderType} 必须走标准装配，且不得挂自定义 shader——
 *       Iris 对自己的光影程序之外的类型支持有限。</li>
 *   <li><b>实体渲染用标准渲染器。</b>实体一律注册成
 *       {@link net.minecraft.client.renderer.entity.EntityRenderer} 子类，通过
 *       {@code EntityRenderersEvent.RegisterRenderers} 注册。Sodium 不接管实体渲染，所以这条
 *       相对安全，但模型与贴图仍要经 {@code MultiBufferSource} 输出。</li>
 * </ol>
 *
 * <p>另有一条纪律：<b>不得硬依赖 Iris / Sodium 的 API</b>。本项目不把它们写进依赖，若将来确实
 * 需要判断光影是否开启，只能通过软依赖方式（{@code ModList.get().isLoaded("iris")} 之后再取用），
 * 且不得让缺少它们时崩溃。
 *
 * <p>本包内的渲染器（{@code CrownCurioRenderer}、{@code FROrbRenderer}、
 * {@code FRBabylonWeaponRenderer}、{@code FRShinyEnergyRenderer}）都只用了上面第二条允许的写法
 * （{@code MultiBufferSource} + 原版 {@code RenderType}），整个项目没有任何一处
 * {@code GlStateManager}/{@code RenderSystem}/{@code BufferBuilder} 调用。
 *
 * <p>另有一个例外需要说明：{@link com.beiwu.forgottenrelics_plus.client.FRParticles} 不在三条约束的
 * 射程内——它只是「造 Botania 的 {@code SparkleParticleData}/{@code WispParticleData} 并调用
 * {@code Level#addParticle} / {@code ServerLevel#sendParticles}」，不碰 GL 状态、不建顶点、
 * 不注册 {@code RenderType}，因此可以被实体 / 物品这类<b>公共端</b>代码直接调用（服务端只会用到
 * 它的 {@code server*} 方法，且这些方法只依赖 Botania 的公共粒子数据类，不依赖任何客户端 MC 类型）。
 *
 * <p>第二处说明：{@link com.beiwu.forgottenrelics_plus.client.FRBolts} 是闪电弧（1.7.10
 * {@code imposeLightning} / RE {@code LightningMessage}）的客户端落点，它本身<b>也不在</b>三条约束的
 * 射程内——它不碰 GL 状态、不建顶点、不注册 {@code RenderType}，只是把端点转交给 Botania 的
 * {@code vazkii.botania.client.fx.BoltRenderer}：折线几何由 Botania 的
 * {@code BoltParticleOptions#generate()} 生成，绘制与 flush 由 Botania 自己的
 * {@code WorldOverlays.renderWorldLast}（{@code LevelRendererMixin} 注入）负责，用的 RenderType
 * {@code RenderHelper.LIGHTNING} 是「标准装配 + 原版 {@code POSITION_COLOR_SHADER}」、没有挂自定义
 * 着色器。它与 {@code FRParticles} 一样，只在客户端被 {@code RegisterParticleProvidersEvent}
 * 注册一次；服务端那半（把端点广播出去）在公共端的
 * {@link com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData} 里，走的是原版粒子包。
 */
package com.beiwu.forgottenrelics_plus.client;
