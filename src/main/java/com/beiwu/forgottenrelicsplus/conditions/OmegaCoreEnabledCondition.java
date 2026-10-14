package com.beiwu.forgottenrelicsplus.conditions;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public record OmegaCoreEnabledCondition() implements ICondition {
    public static final MapCodec<OmegaCoreEnabledCondition> CODEC = MapCodec.unit(new OmegaCoreEnabledCondition());

    @Override
    public boolean test(IContext context) {
        return FRConfig.SPEC.isLoaded() ? FRConfig.OMEGA_CORE_ENABLED.get() : FRConfig.OMEGA_CORE_ENABLED.getDefault();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
