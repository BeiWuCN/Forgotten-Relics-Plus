package com.beiwu.forgottenrelics_plus.client;

import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelics_plus.registry.FRParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import vazkii.botania.client.fx.BoltParticleOptions;
import vazkii.botania.client.fx.BoltParticleOptions.BoltRenderInfo;
import vazkii.botania.client.fx.BoltRenderer;

/**
 * 闪电弧的<b>客户端</b>渲染入口——把 {@link FRBoltParticleData} 转交给 Botania 的闪电渲染器。
 *
 * <h2>1.7.10 / RE 怎么做 → 我们怎么做</h2>
 *
 * <p>1.7.10 的 {@code Main.proxy.lightning(...)} 与 RE 的 {@code LightningMessage} 处理器最后都
 * 落到 Thaumcraft 的 {@code FXLightningBolt} / {@code FXDispatcher.arcBolt}——一个自己生成折线几何、
 * 加法混合绘制的电弧。1.21.1 的 Botania 有一模一样的现代对应物，它自己就在用：
 * {@code ClientProxy#lightningFX} → {@code BoltRenderer.INSTANCE.add(level, options, partialTick)}。
 * 反编译（CFR，{@code Tools/cfr.jar}）确认这条链：
 * <ul>
 *   <li>{@code BoltParticleOptions#generate()} 把折线切成四边形（{@code rightAdd = diff.cross(...)
 *       .normalize().scale(size)}），所以 {@code size} 是四边带的<b>半宽</b>，与 Thaumcraft
 *       {@code setWidth} 同义；</li>
 *   <li>{@code BoltRenderer} 把结果放进自己的发射器列表，每帧用 {@code RenderHelper.LIGHTNING}
 *       写顶点并 {@code endBatch}；那条 RenderType 是「标准装配」的：{@code POSITION_COLOR + QUADS}、
 *       着色器 {@code POSITION_COLOR_SHADER}、混合 {@code LIGHTNING_TRANSPARENCY}
 *       （{@code SRC_ALPHA, ONE}），<b>没有挂 Botania 自己的着色器</b>；</li>
 *   <li>Botania 的 {@code LevelRendererMixin} 无条件注入 {@code renderLevel}，每帧调用
 *       {@code WorldOverlays.renderWorldLast} → {@code BoltRenderer.onWorldRenderLast}。
 *       所以只要往 {@code BoltRenderer} 里登记，Botania 自己就会画、自己就会 flush，
 *       我们<b>不需要</b>注册任何 RenderType，也不需要自己写渲染钩子。</li>
 * </ul>
 *
 * <p>因此这里只做 Botania {@code ClientProxy#lightningFX} 那三行；{@code size} 与颜色按调用点给
 * （RE 主弧 0.075 / 链式 0.04，颜色 0.4/0.6/1.0），不用把 width 与颜色都写死的门面
 * {@code Proxy#lightningFX}。
 *
 * <h2>它为什么是一个「粒子 provider」</h2>
 *
 * <p>Botania 的 {@code BoltRenderer} 没有对应的 {@code ParticleType}，原版粒子包里也没有，
 * 所以「服务端怎么把两端告诉客户端」这件事，本项目用了一个不画任何东西的自定义粒子
 * （{@link FRBoltParticleData}）来承载：服务端 {@code sendParticles} 广播，客户端 provider 收到后
 * 立刻翻译成一次 {@code BoltRenderer} 调用并返回 {@code null}（不产生真正的粒子）。
 * 这样<b>不新增任何自定义网络包</b>。
 *
 * <p>与 {@code client/package-info.java} 三条硬约束的关系：本类不碰 GL 状态、不自己建
 * {@code VertexConsumer}、不注册 {@code RenderType}，顶点、RenderType 与 flush 全部由 Botania
 * 负责，而它用的正是「标准装配 + 原版 shader」。
 */
public final class FRBolts {

    /**
     * 电弧顶点 alpha，取 Botania {@code BoltRenderInfo.DEFAULT} 的 {@code 0.8}。
     *
     * <p>{@code LIGHTNING_TRANSPARENCY} 是 {@code SRC_ALPHA, ONE}，alpha 会参与混合，
     * 所以这个值直接决定电弧的亮度；不动它 = 和 Botania 自己的落雷一样亮。
     */
    private static final float ALPHA = 0.8F;

    /**
     * 电弧存活时长（tick）——它就是观感上的「绘制速度」。
     *
     * <p>{@code BoltParticleOptions} 默认带 {@code FadeFunction.fade(0.5)}：弧头在前 50% 寿命里
     * 从起点爬到终点，后 50% 收尾，所以<b>弧头画到终点的时间 = lifespan / 2</b>。Botania 默认
     * {@code 30} → 15 tick（0.75 秒），慢得像在「长」出来。
     *
     * <p>这里取 {@code 8}：<b>4 tick（约 0.2 秒）画到终点</b>，之后整道弧还会亮满
     * {@code 8 - 4 = 4} tick 才收尾。想要更接近「瞬间全亮」就把这里调到 {@code 6}（3 tick 画完）。
     */
    private static final int LIFESPAN_TICKS = 8;

    public static void registerProvider(RegisterParticleProvidersEvent event) {
        event.registerSpecial(FRParticleTypes.BOLT.get(), FRBolts::createBolt);
    }

    /**
     * 「闪电粒子」的 provider：不造粒子，只登记一道电弧（就是 Botania
     * {@code ClientProxy#lightningFX} 的三行，逐字对应）。
     *
     * @return 恒为 {@code null}——真正的画面由 Botania 的 {@link BoltRenderer} 在
     *         {@code WorldOverlays.renderWorldLast} 里画，这里返回任何粒子都会多画一层。
     */
    @Nullable
    private static Particle createBolt(FRBoltParticleData data, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
        BoltRenderInfo info = BoltRenderInfo.DEFAULT
                .color(new Vector4f(data.red(), data.green(), data.blue(), ALPHA));
        BoltParticleOptions options = new BoltParticleOptions(info, data.from(), data.to())
                .count(data.count())
                .size(data.width())
                // 电弧动画时长；默认 fade(0.5) 下「画到终点」= lifespan / 2 tick，见 LIFESPAN_TICKS。
                .lifespan(LIFESPAN_TICKS)
                // Botania 默认的 SpawnFunction 是「60 tick 后再生」；对一次性电弧来说，
                // 世界时间不足 60 tick 时（刚进世界那几秒）会不显示，这里显式改成无延迟。
                .spawn(BoltParticleOptions.SpawnFunction.NO_DELAY);
        BoltRenderer.INSTANCE.add(level, options,
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        return null;
    }

    private FRBolts() {
    }
}
