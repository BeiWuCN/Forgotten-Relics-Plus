package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.particle.FRSpellParticleData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<FRBoltParticleData>> BOLT =
            PARTICLE_TYPES.register("bolt", FRParticleTypes::createBoltType);
    public static final DeferredHolder<ParticleType<?>, ParticleType<FRSpellParticleData>> WISP =
            PARTICLE_TYPES.register("spell_wisp", FRParticleTypes::createSpellType);
    public static final DeferredHolder<ParticleType<?>, ParticleType<FRSpellParticleData>> SPARKLE =
            PARTICLE_TYPES.register("spell_sparkle", FRParticleTypes::createSpellType);

    private static ParticleType<FRSpellParticleData> createSpellType() {
        return new ParticleType<FRSpellParticleData>(false) {
            @Override
            public MapCodec<FRSpellParticleData> codec() {
                return FRSpellParticleData.CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, FRSpellParticleData> streamCodec() {
                return FRSpellParticleData.STREAM_CODEC;
            }
        };
    }

    private static ParticleType<FRBoltParticleData> createBoltType() {
        return new ParticleType<FRBoltParticleData>(true) {
            @Override
            public MapCodec<FRBoltParticleData> codec() {
                return FRBoltParticleData.CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, FRBoltParticleData> streamCodec() {
                return FRBoltParticleData.STREAM_CODEC;
            }
        };
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }

    private FRParticleTypes() {
    }
}
