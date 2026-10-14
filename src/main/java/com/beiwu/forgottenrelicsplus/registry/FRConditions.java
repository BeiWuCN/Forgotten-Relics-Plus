package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.conditions.OmegaCoreEnabledCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class FRConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, ForgottenRelics.MOD_ID);

    static {
        CONDITIONS.register("omega_core_enabled", () -> OmegaCoreEnabledCondition.CODEC);
    }

    private FRConditions() {}

    public static void register(IEventBus modBus) {
        CONDITIONS.register(modBus);
    }
}
