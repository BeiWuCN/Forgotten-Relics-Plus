package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 粒子类型注册表。
 *
 * <p>本项目<b>只有一个</b>自定义粒子类型：{@link #BOLT}「闪电弧」。它不是用来画粒子的——
 * 它只是 1.7.10 {@code LightningMessage} 那个自定义网络包的现代替身：把闪电两端塞进
 * {@link net.minecraft.core.particles.ParticleOptions}，借原版 {@code ClientboundLevelParticlesPacket}
 * 广播到客户端，客户端的 provider 再把端点交给 Botania 的 {@code BoltRenderer} 画成真正的折线闪电。
 * 详见 {@link FRBoltParticleData} 与 {@code client/FRBolts}。
 *
 * <p>类型本身在<b>两端</b>都要注册（服务端要能编码，客户端要能解码并找到 provider），
 * 所以走公共端的 {@link DeferredRegister}；provider 只在客户端注册。
 *
 * <p>注意：{@code ParticleType} 的 {@code overrideLimiter} 传 {@code true}。这不是为了好看——
 * {@code LevelRenderer#addParticleInternal} 在「粒子：最少」时会把普通粒子直接丢掉，
 * 而那正是客户端粒子 provider 唯一被调用的入口；置 {@code true} 才能保证闪电在任何粒子设置下都出现。
 * 同时这个类型<b>不能</b>再配 {@code assets/.../particles/bolt.json}，否则 NeoForge 会报
 * 「重复的贴图列表」（见 {@code RegisterParticleProvidersEvent#registerSpecial} 的说明）。
 */
public final class FRParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ForgottenRelics.MOD_ID);

    /** 闪电弧载体，见 {@link FRBoltParticleData}。 */
    public static final DeferredHolder<ParticleType<?>, ParticleType<FRBoltParticleData>> BOLT =
            PARTICLE_TYPES.register("bolt", FRParticleTypes::createBoltType);

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
