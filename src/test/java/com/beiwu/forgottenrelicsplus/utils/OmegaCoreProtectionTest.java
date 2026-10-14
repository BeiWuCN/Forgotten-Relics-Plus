package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;

class OmegaCoreProtectionTest {
    @Test
    void voidLimitFollowsEachDimensionsMinimumHeight() {
        assertFalse(OmegaCoreProtection.belowVoidLimit(-320, -64));
        assertTrue(OmegaCoreProtection.belowVoidLimit(-320.01D, -64));
        assertFalse(OmegaCoreProtection.belowVoidLimit(-256, 0));
        assertTrue(OmegaCoreProtection.belowVoidLimit(-257, 0));
        assertFalse(OmegaCoreProtection.belowVoidLimit(-319, -64));
    }
    @Test
    void zeroNegativeAndNonfiniteHealthRequireRepair() {
        for (float health : new float[] {0, -1, -Float.MAX_VALUE, Float.NaN,
                Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY}) {
            assertTrue(OmegaCoreProtection.needsHealthRepair(health));
        }
        assertFalse(OmegaCoreProtection.needsHealthRepair(0.1F));
        assertFalse(OmegaCoreProtection.needsHealthRepair(20));
    }

    @Test
    void removalPolicyBlocksDeathButAllowsWorldLifecycle() {
        assertTrue(OmegaCoreProtection.blocksRemoval(Entity.RemovalReason.KILLED));
        assertTrue(OmegaCoreProtection.blocksRemoval(Entity.RemovalReason.DISCARDED));
        assertFalse(OmegaCoreProtection.blocksRemoval(Entity.RemovalReason.UNLOADED_TO_CHUNK));
        assertFalse(OmegaCoreProtection.blocksRemoval(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        assertFalse(OmegaCoreProtection.blocksRemoval(Entity.RemovalReason.CHANGED_DIMENSION));
    }
}
