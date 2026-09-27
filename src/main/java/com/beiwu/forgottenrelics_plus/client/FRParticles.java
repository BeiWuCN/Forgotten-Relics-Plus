package com.beiwu.forgottenrelics_plus.client;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

/**
 * Botania 粒子（sparkle / wisp）在本移植里的唯一调用入口。
 *
 * <h2>与旧版 Botania 粒子代理的对应</h2>
 *
 * <p>1.7.10 原版与 RE 直接调 Botania 的粒子代理，本移植改用它们的现代对应物：
 * <pre>
 *   Botania.proxy.sparkleFX(world, x, y, z, r, g, b, size, m);
 *   Botania.proxy.wispFX(world, x, y, z, r, g, b, size, xm, ym, zm[, maxAgeMul]);
 * </pre>
 * 参数语义已用 javap + CFR 反编译 {@code libs/botania-neoforge-1.21.1-457.jar} 核对：
 * <ul>
 *   <li>{@code sparkle(..., size, m)} → {@link SparkleParticleData#sparkle(float, float, float, float, int)}：
 *       {@code quadSize = (rand*0.5+0.5) * 0.2 * size}、{@code lifetime = 3 * m}、{@code alpha = 0.75}、
 *       初速恒为 0。<b>{@code m} 是寿命倍率（存活 3×m tick），不是速度也不是数量</b>。</li>
 *   <li>{@code wisp(..., size, xm, ym, zm[, maxAgeMul])} → {@link WispParticleData#wisp}：
 *       {@code quadSize = (rand*0.5+0.5) * 2.0 * size}、{@code alpha = 0.375}、
 *       {@code lifetime = maxAgeMul * 28 / (rand*0.3 + 0.7)}、{@code gravity} 默认 0。
 *       {@code xm / ym / zm} 是粒子的初始速度，由 {@code Level#addParticle} 的第 4~6 个参数
 *       （或 {@code sendParticles} 的 count==0 分支）传入。</li>
 * </ul>
 *
 * <p>旧版成对出现的 {@code setWispFXDistanceLimit(false/true)} 与 {@code setSparkleFXCorrupt(false)}
 * 是粒子代理上的全局开关；1.21.1 的 Botania 已把它们去掉，粒子一律按原版渲染距离规则绘制，
 * 因此本类不提供对应方法。
 *
 * <h2>客户端 / 服务端</h2>
 * <ul>
 *   <li>{@link #sparkle} / {@link #wisp} 只用于客户端路径（拖尾、结界环），零网络开销；</li>
 *   <li>{@link #serverSparkle} / {@link #serverWisp} / {@link #serverWispBurst} 用于服务端一次性效果，
 *       由 {@code ServerLevel#sendParticles} 广播。</li>
 * </ul>
 *
 * <p>sendParticles 的两种模式（已核 1.21.1 {@code ClientPacketListener#handleParticleEvent}）：
 * {@code count == 0} 时发一颗、位置就是 (x,y,z)、初速 = {@code maxSpeed * (xDist,yDist,zDist)}；
 * {@code count > 0} 时发 count 颗，位置 = (x,y,z) + gaussian × (xDist,yDist,zDist)、
 * 初速 = gaussian × maxSpeed。本类的 {@code serverWisp} 走第一种（单颗、能精确给初速），
 * {@code serverWispBurst} 走第二种（一簇、只给整体的扩散与速度）。
 *
 * <p>本类只做「造粒子数据 + 发出」，不碰 GL 状态、不建顶点、不注册 RenderType，
 * 因此不触发 {@code client/package-info.java} 里那三条 Sodium / Iris 约束。
 */
public final class FRParticles {

    private FRParticles() {
    }

    // ---- 客户端：逐颗、可精确指定初速 ----

    /**
     * 客户端 sparkle，对应 {@code Botania.proxy.sparkleFX(world, x, y, z, r, g, b, size, m)}。
     *
     * <p>没有初速参数——旧版 sparkleFX 同样不给速度，粒子恒定在原地闪烁 3×m tick。
     */
    public static void sparkle(Level level, double x, double y, double z,
                               float r, float g, float b, float size, int m) {
        level.addParticle(SparkleParticleData.sparkle(size, r, g, b, m), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    /**
     * 客户端 wisp，对应 {@code wispFX(world, x, y, z, r, g, b, size, xm, ym, zm)}（maxAgeMul = 1.0）。
     */
    public static void wisp(Level level, double x, double y, double z,
                            float r, float g, float b, float size,
                            double xm, double ym, double zm) {
        wisp(level, x, y, z, r, g, b, size, xm, ym, zm, 1.0F);
    }

    /**
     * 客户端 wisp，对应 {@code wispFX(world, x, y, z, r, g, b, size, xm, ym, zm, maxAgeMul)}。
     *
     * <p>{@code maxAgeMul} 就是反编译里那个字段：寿命 = {@code maxAgeMul * 28 / (rand*0.3 + 0.7)} tick。
     * 调用点里 0.45 / 0.5 / 0.6 / 0.8 / 0.9 / 1.0 这些取值全部是寿命倍率。
     */
    public static void wisp(Level level, double x, double y, double z,
                            float r, float g, float b, float size,
                            double xm, double ym, double zm, float maxAgeMul) {
        level.addParticle(WispParticleData.wisp(size, r, g, b, maxAgeMul), x, y, z, xm, ym, zm);
    }

    // ---- 服务端：一次性爆发 ----

    /** 服务端单颗 sparkle（count == 0 分支），用于命中点的那一颗「爆闪」。 */
    public static void serverSparkle(ServerLevel level, double x, double y, double z,
                                     float r, float g, float b, float size, int m) {
        level.sendParticles(SparkleParticleData.sparkle(size, r, g, b, m), x, y, z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
    }

    /**
     * 服务端单颗 wisp、带精确初速（count == 0 分支：初速 = maxSpeed(1.0) × (xm, ym, zm)）。
     *
     * <p>用于「每颗颜色 / 初速都不同」的旧客户端循环（收束型粒子、逐颗随机色），
     * 一次调用对应客户端循环里的一次 wispFX。
     */
    public static void serverWisp(ServerLevel level, double x, double y, double z,
                                  float r, float g, float b, float size,
                                  double xm, double ym, double zm) {
        serverWisp(level, x, y, z, r, g, b, size, xm, ym, zm, 1.0F);
    }

    /** 同 {@link #serverWisp}，但可指定寿命倍率 maxAgeMul。 */
    public static void serverWisp(ServerLevel level, double x, double y, double z,
                                  float r, float g, float b, float size,
                                  double xm, double ym, double zm, float maxAgeMul) {
        level.sendParticles(WispParticleData.wisp(size, r, g, b, maxAgeMul), x, y, z, 0, xm, ym, zm, 1.0D);
    }

    /**
     * 服务端一簇 wisp（count &gt; 0 分支）：位置在 (x,y,z) 周围按 gaussian × spread 抖开，
     * 初速按 gaussian × speed 抖开，整簇共用同一个 ParticleOptions。
     *
     * <p>旧版是「一个网络包 → 客户端 for 循环、每颗各自随机颜色」，本移植改成服务端随机：
     * 颜色 / 尺寸在调用处只抽一次（落在原版给出的随机区间内），逐颗随机的差异记在各调用点；
     * 换来「一次爆发只发一个包」，与原版的网络开销同量级。
     *
     * @param count  粒子数
     * @param spread 位置抖动（gaussian 标准差）
     * @param speed  初速抖动（gaussian 标准差）；
     *               旧版 {@code xm = (rand - 0.5) * m} 的 m 换算过来约为 {@code m / sqrt(12) ≈ 0.289m}
     */
    public static void serverWispBurst(ServerLevel level, double x, double y, double z,
                                       float r, float g, float b, float size, float maxAgeMul,
                                       int count, double spread, double speed) {
        level.sendParticles(WispParticleData.wisp(size, r, g, b, maxAgeMul),
                x, y, z, count, spread, spread, spread, speed);
    }
}
