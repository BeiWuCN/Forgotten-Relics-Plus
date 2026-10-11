package com.beiwu.forgottenrelicsplus.particle;

import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
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

public record FRBoltParticleData(Vec3 from, Vec3 to, float width, int count, int color,
                                 int fromEntityId, int toEntityId) implements ParticleOptions {

    public FRBoltParticleData(Vec3 from, Vec3 to, float width, int count, int color) {
        this(from, to, width, count, color, -1, -1);
    }

    public static final double BROADCAST_RADIUS = 64.0D;

    public static final float ARC_RED = 0.4F;
    public static final float ARC_GREEN = 0.6F;
    public static final float ARC_BLUE = 1.0F;

    public static final float OBELISK_ARC_RED = 1.0F;
    public static final float OBELISK_ARC_GREEN = 0.6F;
    public static final float OBELISK_ARC_BLUE = 1.0F;

    public static final float WIDTH_MAIN = 0.075F;
    public static final float WIDTH_CHAIN = 0.04F;

    public static final MapCodec<FRBoltParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Vec3.CODEC.fieldOf("from").forGetter(FRBoltParticleData::from),
                    Vec3.CODEC.fieldOf("to").forGetter(FRBoltParticleData::to),
                    Codec.FLOAT.fieldOf("width").forGetter(FRBoltParticleData::width),
                    Codec.INT.fieldOf("count").forGetter(FRBoltParticleData::count),
                    Codec.INT.fieldOf("color").forGetter(FRBoltParticleData::color),
                    Codec.INT.optionalFieldOf("from_entity", -1).forGetter(FRBoltParticleData::fromEntityId),
                    Codec.INT.optionalFieldOf("to_entity", -1).forGetter(FRBoltParticleData::toEntityId))
            .apply(instance, FRBoltParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FRBoltParticleData> STREAM_CODEC =
            StreamCodec.of(FRBoltParticleData::write, FRBoltParticleData::read);

    private static void write(RegistryFriendlyByteBuf buffer, FRBoltParticleData data) {
        writeVec(buffer, data.from);
        writeVec(buffer, data.to);
        buffer.writeFloat(data.width);
        buffer.writeInt(data.count);
        buffer.writeInt(data.color);
        buffer.writeInt(data.fromEntityId);
        buffer.writeInt(data.toEntityId);
    }

    private static FRBoltParticleData read(RegistryFriendlyByteBuf buffer) {
        return new FRBoltParticleData(readVec(buffer), readVec(buffer),
                buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
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

    public static void broadcast(ServerLevel level, Vec3 from, Vec3 to, float width, int count,
                                 float red, float green, float blue) {
        broadcast(level, from, to, width, count, red, green, blue, -1, -1);
    }

    public static void broadcast(ServerLevel level, Vec3 from, Vec3 to, float width, int count,
                                 float red, float green, float blue, int fromEntityId, int toEntityId) {

        if (from.distanceToSqr(to) < 1.0E-6D) {
            return;
        }
        var data = new FRBoltParticleData(from, to, width, Math.max(1, count), packColor(red, green, blue),
                fromEntityId, toEntityId);
        List<ServerPlayer> players = level.players();
        for (ServerPlayer player : players) {
            if (player.position().distanceToSqr(from) <= BROADCAST_RADIUS * BROADCAST_RADIUS) {

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
