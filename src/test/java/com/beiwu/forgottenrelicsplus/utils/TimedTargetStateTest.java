package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TimedTargetStateTest {
    @Test
    void dimensionResetInvalidatesTheTargetWithoutResettingCooldown() {
        TimedTargetState state = new TimedTargetState();
        state.target = 17;
        state.ticksTillExpire = 5;
        state.ticksCooldown = 40;
        state.clearTarget();
        assertEquals(-1, state.target);
        assertEquals(0, state.ticksTillExpire);
        assertEquals(40, state.ticksCooldown);
        assertFalse(state.isInactive());
    }

    @Test
    void targetExpiresAndCooldownContinuesWithoutAnItem() {
        TimedTargetState state = new TimedTargetState();
        state.target = 42;
        state.dist = 7.5;
        state.reDist = 12;
        state.ticksTillExpire = 2;
        state.ticksCooldown = 4;
        state.tick();
        assertEquals(42, state.target);
        state.tick();
        assertEquals(-1, state.target);
        assertEquals(-1.0, state.dist);
        assertEquals(-1.0, state.reDist);
        assertFalse(state.isInactive());
        state.tick();
        state.tick();
        assertTrue(state.isInactive());
        for (int i = 0; i < 100; i++) {
            state.tick();
        }
        assertEquals(0, state.ticksTillExpire);
        assertEquals(0, state.ticksCooldown);
    }
}
