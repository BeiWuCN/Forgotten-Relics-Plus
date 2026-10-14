package net.neoforged.fml.config;

import static org.junit.jupiter.api.Assertions.*;

import com.beiwu.forgottenrelicsplus.conditions.OmegaCoreEnabledCondition;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.serialization.JsonOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.junit.jupiter.api.Test;

class OmegaCoreEnabledConditionTest {
    @Test
    void conditionUsesDefaultThenRespectsEnabledAndDisabledConfig() {
        var condition = new OmegaCoreEnabledCondition();
        assertTrue(condition.test(ICondition.IContext.EMPTY));
        CommentedConfig config = CommentedConfig.inMemory();
        FRConfig.SPEC.correct(config);
        config.set(FRConfig.OMEGA_CORE_ENABLED.getPath(), false);
        try {
            FRConfig.SPEC.acceptConfig(new LoadedConfig(config, null, null));
            assertFalse(condition.test(ICondition.IContext.EMPTY));
            FRConfig.OMEGA_CORE_ENABLED.set(true);
            assertFalse(condition.test(ICondition.IContext.EMPTY));
            FRConfig.OMEGA_CORE_ENABLED.clearCache();
            assertTrue(condition.test(ICondition.IContext.EMPTY));
            FRConfig.OMEGA_CORE_ENABLED.set(false);
            assertTrue(condition.test(ICondition.IContext.EMPTY));
            FRConfig.OMEGA_CORE_ENABLED.clearCache();
            assertFalse(condition.test(ICondition.IContext.EMPTY));
        } finally {
            FRConfig.SPEC.acceptConfig(null);
        }
        assertTrue(condition.test(ICondition.IContext.EMPTY));
    }

    @Test
    void conditionCodecRoundTripsWithoutConfigSnapshot() {
        var condition = new OmegaCoreEnabledCondition();
        var json = OmegaCoreEnabledCondition.CODEC.codec().encodeStart(JsonOps.INSTANCE, condition).getOrThrow();
        assertEquals(condition, OmegaCoreEnabledCondition.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
