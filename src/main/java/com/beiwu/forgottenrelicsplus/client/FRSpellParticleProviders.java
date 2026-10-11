package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import vazkii.botania.client.fx.FXSparkle;
import vazkii.botania.client.fx.FXWisp;

public final class FRSpellParticleProviders {
    private FRSpellParticleProviders() {}

    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FRParticleTypes.WISP.get(), sprites -> (data, level, x, y, z, dx, dy, dz) -> {
            FXWisp particle = new FXWisp(level, x, y, z, dx, dy, dz,
                    data.size(), data.red(), data.green(), data.blue(), true, data.duration(), true, 0) {
                @Override
                public ParticleRenderType getRenderType() {
                    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
                }

                @Override
                protected int getLightColor(float partialTick) {
                    return LightTexture.FULL_BRIGHT;
                }
            };
            particle.pickSprite(sprites);
            return particle;
        });
        event.registerSpriteSet(FRParticleTypes.SPARKLE.get(), sprites -> (data, level, x, y, z, dx, dy, dz) ->
                new FXSparkle(level, x, y, z, data.size(), data.red(), data.green(), data.blue(),
                        Math.max(1, (int) data.duration()), false, true, false, sprites) {
                    @Override
                    public ParticleRenderType getRenderType() {
                        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
                    }

                    @Override
                    protected int getLightColor(float partialTick) {
                        return LightTexture.FULL_BRIGHT;
                    }
                });
    }
}
