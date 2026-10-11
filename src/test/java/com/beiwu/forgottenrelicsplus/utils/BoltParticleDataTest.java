package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class BoltParticleDataTest {
    @Test
    void attachedEndpointIdsSurviveNetworkRoundTrip() {
        var data = new FRBoltParticleData(new Vec3(10, 20, 30), new Vec3(15, 25, 35),
                0.075F, 1, 0x6699FF, 123, 456);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            FRBoltParticleData.STREAM_CODEC.encode(buffer, data);
            assertEquals(data, FRBoltParticleData.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void staticBoltsRetainUnattachedEndpoints() {
        var data = new FRBoltParticleData(Vec3.ZERO, new Vec3(1, 1, 1), 0.04F, 1, 0xFFFFFF);
        assertEquals(-1, data.fromEntityId());
        assertEquals(-1, data.toEntityId());
        var json = FRBoltParticleData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, FRBoltParticleData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void oldParticleCommandsDecodeWithoutAttachmentFields() {
        var data = new FRBoltParticleData(Vec3.ZERO, new Vec3(1, 1, 1), 0.04F, 1, 0xFFFFFF);
        var json = FRBoltParticleData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow().getAsJsonObject();
        json.remove("from_entity");
        json.remove("to_entity");
        assertEquals(data, FRBoltParticleData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
