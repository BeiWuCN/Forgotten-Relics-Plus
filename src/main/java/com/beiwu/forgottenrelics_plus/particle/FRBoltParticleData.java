package com.beiwu.forgottenrelics_plus.particle;

import com.beiwu.forgottenrelics_plus.registry.FRParticleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 「一道闪电弧」的数据载体，对应 1.7.10 原版 {@code SuperpositionHandler.imposeLightning} /
 * {@code imposeArcLightning} 打包进 {@code LightningMessage} / {@code ArcLightningMessage} 的字段。
 *
 * <h2>为什么是 {@link ParticleOptions}，而不是网络包</h2>
 * <p>1.7.10 与 1.12.2 移植版（RE）都自己写了 {@code LightningMessage}（客户端收到后调 Thaumcraft 的
 * {@code arcBolt} 画电弧）。1.21.1 的 Botania 已带现成的闪电几何生成器与渲染器（见 {@code client/FRBolts}），
 * 缺的只是「把端点送到客户端」。本项目<b>不新写自定义网络包</b>：把这几个字段塞进自定义
 * {@link ParticleOptions}，走原版 {@code ClientboundLevelParticlesPacket}（{@code ServerLevel#sendParticles}）
 * 广播。网络上传输的仍是原版粒子包，只是这个「粒子」自己不画东西（provider 返回 {@code null}），
 * 真正画闪电的是 Botania 的 {@code BoltRenderer}。
 *
 * <h2>字段与原版的对应</h2>
 * <ul>
 *   <li>{@code from} / {@code to}：闪电两端，对应 {@code LightningMessage(x,y,z,destx,desty,destz)}；</li>
 *   <li>{@code width}：电弧粗细，对应 {@code imposeLightning(..., width)} / {@code ArcLightningMessage(..., h)}，
 *       落到 Botania 是 {@code BoltParticleOptions#size(float)}（四边带半宽，与 Thaumcraft {@code setWidth} 同义）；</li>
 *   <li>{@code count}：一次画几股。原版 {@code for (counter = 0; counter <= 3; ++counter)} 的「同一处连画 4 次」
 *       换算成 {@code BoltParticleOptions#count(int)}，一次广播就是 4 股随机折线，省掉 4 个粒子包；</li>
 *   <li>{@code color}：{@code 0xRRGGBB}，对应 {@code ArcLightningMessage} 的 r/g/b；{@code imposeLightning}
 *       没有颜色参数，统一用 RE 客户端 {@code arcBolt(..., 0.4, 0.6, 1.0)} 的电蓝。</li>
 * </ul>
 *
 * <p>原版还有 {@code duration} / {@code curve} / {@code speed} / {@code type}：前三者由 Botania 的
 * {@code BoltRenderInfo}（{@code spreadFactor} 默认 0.1）与 {@code lifespan}（默认 30 tick）承担，
 * 这里沿用 Botania 默认值（与 Botania 自己的 {@code Proxy#lightningFX} 一致）；{@code type} 是 Thaumcraft
 * 的闪电变体编号，1.21.1 无对应物，忽略。
 *
 * <p>本类只做「打包 + 服务端广播」，不碰任何客户端类型，可被物品 / 实体这类公共端代码直接调用。
 */
public record FRBoltParticleData(Vec3 from, Vec3 to, float width, int count, int color) implements ParticleOptions {

    /** 广播半径（格）：对应原版 {@code sendToAllAround(..., 64.0)}（RE）与 {@code 128.0}（1.7.10）。 */
    public static final double BROADCAST_RADIUS = 64.0D;

    /** RE 客户端 {@code arcBolt(..., 0.4F, 0.6F, 1.0F)} 的电蓝色，原版 {@code imposeLightning} 各处沿用。 */
    public static final float ARC_RED = 0.4F;
    public static final float ARC_GREEN = 0.6F;
    public static final float ARC_BLUE = 1.0F;

    /** 1.7.10 {@code imposeArcLightning(..., 1.0F, 0.6F, 1.0F, h)} 的粉紫色，只用于方尖碑四周的随机弧光。 */
    public static final float OBELISK_ARC_RED = 1.0F;
    public static final float OBELISK_ARC_GREEN = 0.6F;
    public static final float OBELISK_ARC_BLUE = 1.0F;

    /** 原版 {@code shootLightning(..., main)}：主电弧 0.075、链式 0.04。 */
    public static final float WIDTH_MAIN = 0.075F;
    public static final float WIDTH_CHAIN = 0.04F;

    /** JSON 编解码（{@code ParticleTypes.CODEC} 派发用）。网络不走它，但 {@link ParticleType} 要求提供。 */
    public static final MapCodec<FRBoltParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Vec3.CODEC.fieldOf("from").forGetter(FRBoltParticleData::from),
                    Vec3.CODEC.fieldOf("to").forGetter(FRBoltParticleData::to),
                    Codec.FLOAT.fieldOf("width").forGetter(FRBoltParticleData::width),
                    Codec.INT.fieldOf("count").forGetter(FRBoltParticleData::count),
                    Codec.INT.fieldOf("color").forGetter(FRBoltParticleData::color))
            .apply(instance, FRBoltParticleData::new));

    /**
     * 网络编解码：原版 {@code LightningMessage} 逐字段写 double/double/double/boolean/float 的现代版。
     *
     * <p>手写而不是 {@code StreamCodec.composite}——这里一共 8 个基本字段，
     * 手写既避开 composite 的参数上限，也和原版那一串 {@code buf.readDouble()} 一一对应，更好核对。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FRBoltParticleData> STREAM_CODEC =
            StreamCodec.of(FRBoltParticleData::write, FRBoltParticleData::read);

    private static void write(RegistryFriendlyByteBuf buffer, FRBoltParticleData data) {
        writeVec(buffer, data.from);
        writeVec(buffer, data.to);
        buffer.writeFloat(data.width);
        buffer.writeInt(data.count);
        buffer.writeInt(data.color);
    }

    private static FRBoltParticleData read(RegistryFriendlyByteBuf buffer) {
        return new FRBoltParticleData(readVec(buffer), readVec(buffer),
                buffer.readFloat(), buffer.readInt(), buffer.readInt());
    }

    private static void writeVec(RegistryFriendlyByteBuf buffer, Vec3 vec) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    private static Vec3 readVec(RegistryFriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public ParticleType<FRBoltParticleData> getType() {
        return FRParticleTypes.BOLT.get();
    }

    public float red() {
        return ((this.color >> 16) & 0xFF) / 255.0F;
    }

    public float green() {
        return ((this.color >> 8) & 0xFF) / 255.0F;
    }

    public float blue() {
        return (this.color & 0xFF) / 255.0F;
    }

    /**
     * 服务端广播一道（或 {@code count} 股，用 {@code count} 区分）闪电弧。
     *
     * <p>对应原版 {@code SuperpositionHandler.imposeLightning} / {@code imposeArcLightning}
     * 末尾那次 {@code sendToAllAround(..., 64.0)}：1.7.10 用的是 128 格，RE 收窄到 64；
     * 这里取 RE 的 64 格——注意<b>必须自己按半径筛人</b>，因为
     * {@code ServerLevel#sendParticles(type, x, y, z, ...)} 的硬编码半径只有 32 格。
     *
     * @param level  服务端世界
     * @param from   闪电起点
     * @param to     闪电终点
     * @param width  电弧半宽（格），见 {@link #WIDTH_MAIN} / {@link #WIDTH_CHAIN}
     * @param count  一次画几股折线（原版「连画 4 次」的等价写法）
     * @param red    颜色红分量 0~1
     * @param green  颜色绿分量 0~1
     * @param blue   颜色蓝分量 0~1
     */
    public static void broadcast(ServerLevel level, Vec3 from, Vec3 to, float width, int count,
                                 float red, float green, float blue) {
        // 起终点重合时 Botania 的 segments 会算成 0，直接跳过（广播也没有意义）。
        if (from.distanceToSqr(to) < 1.0E-6D) {
            return;
        }
        var data = new FRBoltParticleData(from, to, width, Math.max(1, count), packColor(red, green, blue));
        List<ServerPlayer> players = level.players();
        for (ServerPlayer player : players) {
            if (player.position().distanceToSqr(from) <= BROADCAST_RADIUS * BROADCAST_RADIUS) {
                // longDistance=true：直接复用「远距离粒子」这条通道，服务端不再做 32 格裁切
                // （半径已由上面那行自己控制），同时让客户端忽略「粒子：最少」设置——闪电属于技能判定的一部分表现。
                level.sendParticles(player, data, true, from.x, from.y, from.z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static int packColor(float red, float green, float blue) {
        int r = (int) (Mth.clamp(red, 0.0F, 1.0F) * 255.0F);
        int g = (int) (Mth.clamp(green, 0.0F, 1.0F) * 255.0F);
        int b = (int) (Mth.clamp(blue, 0.0F, 1.0F) * 255.0F);
        return (r << 16) | (g << 8) | b;
    }
}
