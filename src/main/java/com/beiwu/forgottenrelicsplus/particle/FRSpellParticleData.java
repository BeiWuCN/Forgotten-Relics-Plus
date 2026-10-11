package com.beiwu.forgottenrelicsplus.particle;

import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record FRSpellParticleData(float size, float red, float green, float blue, float duration, boolean sparkle)
        implements ParticleOptions {
    public static final MapCodec<FRSpellParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.001F, 16.0F).fieldOf("size").forGetter(FRSpellParticleData::size),
            Codec.floatRange(0.0F, 1.0F).fieldOf("red").forGetter(FRSpellParticleData::red),
            Codec.floatRange(0.0F, 1.0F).fieldOf("green").forGetter(FRSpellParticleData::green),
            Codec.floatRange(0.0F, 1.0F).fieldOf("blue").forGetter(FRSpellParticleData::blue),
            Codec.floatRange(0.01F, 100.0F).fieldOf("duration").forGetter(FRSpellParticleData::duration),
            Codec.BOOL.fieldOf("sparkle").forGetter(FRSpellParticleData::sparkle)
    ).apply(instance, FRSpellParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FRSpellParticleData> STREAM_CODEC = StreamCodec.of(
            (buffer, data) -> {
                buffer.writeFloat(data.size);
                buffer.writeFloat(data.red);
                buffer.writeFloat(data.green);
                buffer.writeFloat(data.blue);
                buffer.writeFloat(data.duration);
                buffer.writeBoolean(data.sparkle);
            }, buffer -> new FRSpellParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                    buffer.readFloat(), buffer.readFloat(), buffer.readBoolean()));

    @Override
    public ParticleType<FRSpellParticleData> getType() {
        return sparkle ? FRParticleTypes.SPARKLE.get() : FRParticleTypes.WISP.get();
    }

    public static FRSpellParticleData sparkle(float size, float r, float g, float b, int duration) {
        return new FRSpellParticleData(size, r, g, b, duration, true);
    }

    public static FRSpellParticleData wisp(float size, float r, float g, float b, float duration) {
        return new FRSpellParticleData(size, r, g, b, duration, false);
    }
}
