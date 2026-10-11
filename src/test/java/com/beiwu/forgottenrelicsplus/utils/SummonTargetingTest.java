package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SummonTargetingTest {
    @Test
    void directlyAimedMonsterTakesPrecedenceOverOtherBosses() {
        assertTrue(SummonTargeting.priority(true, true, 0) > SummonTargeting.priority(false, true, 2));
    }

    @Test
    void forwardEnemiesTakePrecedenceOverBossesBehindPlayer() {
        assertTrue(SummonTargeting.priority(false, true, 0) > SummonTargeting.priority(false, false, 2));
    }

    @Test
    void bossesThenChampionsWinWithinTheSameViewGroup() {
        assertTrue(SummonTargeting.priority(false, true, 2) > SummonTargeting.priority(false, true, 1));
        assertTrue(SummonTargeting.priority(false, true, 1) > SummonTargeting.priority(false, true, 0));
    }
}
