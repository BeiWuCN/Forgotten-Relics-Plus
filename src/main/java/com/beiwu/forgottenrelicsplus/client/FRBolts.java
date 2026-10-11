package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import vazkii.botania.client.fx.BoltParticleOptions;
import vazkii.botania.client.fx.BoltParticleOptions.BoltRenderInfo;
import vazkii.botania.client.fx.BoltRenderer;

public final class FRBolts {

    private static final float ALPHA = 0.8F;

    private static final int LIFESPAN_TICKS = 8;

    public static void registerProvider(RegisterParticleProvidersEvent event) {
        event.registerSpecial(FRParticleTypes.BOLT.get(), FRBolts::createBolt);
    }

    @Nullable
    private static Particle createBolt(FRBoltParticleData data, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
        BoltRenderInfo info = BoltRenderInfo.DEFAULT
                .color(new Vector4f(data.red(), data.green(), data.blue(), ALPHA));
        BoltParticleOptions options = new BoltParticleOptions(info, data.from(), data.to())
                .count(data.count())
                .size(data.width())

                .lifespan(LIFESPAN_TICKS)

                .spawn(BoltParticleOptions.SpawnFunction.NO_DELAY);
        BoltRenderer.INSTANCE.add(level, options,
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        return null;
    }

    private FRBolts() {
    }
}
