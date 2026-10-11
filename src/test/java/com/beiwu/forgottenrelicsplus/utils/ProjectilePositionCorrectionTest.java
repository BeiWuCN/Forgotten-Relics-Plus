package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ProjectilePositionCorrectionTest {
    @Test
    void correctionConvergesWithoutOvershooting() {
        var correction = new ProjectilePositionCorrection();
        Vec3 target = new Vec3(3, -6, 9);
        assertTrue(correction.retarget(target));
        Vec3 sum = Vec3.ZERO;
        for (int tick = 0; tick < 40; tick++) {
            Vec3 step = correction.consume();
            assertTrue(step.x >= 0);
            sum = sum.add(step);
            assertTrue(sum.x <= target.x + 1.0E-6D);
        }
        assertEquals(0.0D, target.subtract(sum).length(), 1.0E-6D);
        assertSame(Vec3.ZERO, correction.consume());
    }

    @Test
    void retargetKeepsCorrectionMomentumAndConvergesToLatestPacket() {
        var correction = new ProjectilePositionCorrection();
        correction.retarget(6, 0, 0);
        correction.consume();
        correction.retarget(0, 0, 0);
        Vec3 sum = correction.consume();
        assertTrue(sum.x > 0);
        for (int tick = 0; tick < 40; tick++) {
            sum = sum.add(correction.consume());
        }
        assertEquals(0.0D, sum.length(), 1.0E-6D);
    }

    @Test
    void teleportClearsOldCorrection() {
        var correction = new ProjectilePositionCorrection();
        correction.retarget(3, 0, 0);
        correction.consume();
        assertFalse(correction.retarget(17, 0, 0));
        assertSame(Vec3.ZERO, correction.consume());
    }

    @Test
    void zeroErrorSkipsCorrection() {
        var correction = new ProjectilePositionCorrection();
        assertTrue(correction.retarget(0, 0, 0));
        assertSame(Vec3.ZERO, correction.consume());
    }

    @Test
    void invalidPositionResetsCorrection() {
        var correction = new ProjectilePositionCorrection();
        correction.retarget(3, 0, 0);
        correction.consume();
        assertFalse(correction.retarget(Double.NaN, 0, 0));
        assertSame(Vec3.ZERO, correction.consume());
        assertFalse(correction.retarget(Double.POSITIVE_INFINITY, 0, 0));
    }
}
