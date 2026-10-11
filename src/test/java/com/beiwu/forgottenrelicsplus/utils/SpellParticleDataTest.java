package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.beiwu.forgottenrelicsplus.particle.FRSpellParticleData;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

class SpellParticleDataTest {
    @Test
    void colorSizeLifetimeAndKindSurviveNetworkRoundTrip() {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            for (var data : new FRSpellParticleData[] {
                    FRSpellParticleData.wisp(0.75F, 0.2F, 0.4F, 1, 2.5F),
                    FRSpellParticleData.sparkle(0.5F, 1, 0.8F, 0.3F, 2)}) {
                FRSpellParticleData.STREAM_CODEC.encode(buffer, data);
                assertEquals(data, FRSpellParticleData.STREAM_CODEC.decode(buffer));
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void particleCommandCodecRoundTrips() {
        var data = FRSpellParticleData.wisp(0.5F, 1, 0.2F, 0.7F, 1.5F);
        var json = FRSpellParticleData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, FRSpellParticleData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void codecRejectsInvalidSizesAndColors() {
        var invalid = FRSpellParticleData.wisp(-1, 2, 0, 0, 1);
        assertTrue(FRSpellParticleData.CODEC.codec().encodeStart(JsonOps.INSTANCE, invalid).error().isPresent());
    }
}
