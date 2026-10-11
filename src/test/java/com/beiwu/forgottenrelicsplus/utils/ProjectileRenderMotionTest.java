package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProjectileRenderMotionTest {
    @Test
    void constantVelocityRemainsLinearAtEveryFrame() {
        var motion = new ProjectileRenderMotion();
        motion.reset(10, -2, 3, 2, -1, 0.5);
        motion.advance(12, -3, 3.5);
        for (int frame = 0; frame <= 100; frame++) {
            double t = frame / 100.0D;
            assertEquals(10 + 2 * t, motion.x(t), 1.0E-12D);
            assertEquals(-2 - t, motion.y(t), 1.0E-12D);
            assertEquals(3 + 0.5 * t, motion.z(t), 1.0E-12D);
        }
    }

    @Test
    void turningKeepsPositionAndVelocityContinuousBetweenTicks() {
        var motion = new ProjectileRenderMotion();
        motion.reset(0, 0, 0, 1, 0, 0);
        motion.advance(1, 0, 0);
        double epsilon = 1.0E-6D;
        double endX = motion.x(1);
        double endY = motion.y(1);
        double incomingX = (endX - motion.x(1 - epsilon)) / epsilon;
        double incomingY = (endY - motion.y(1 - epsilon)) / epsilon;
        motion.advance(1.5, 1, 0);
        assertEquals(endX, motion.x(0), 1.0E-12D);
        assertEquals(endY, motion.y(0), 1.0E-12D);
        assertEquals(incomingX, (motion.x(epsilon) - motion.x(0)) / epsilon, 1.0E-5D);
        assertEquals(incomingY, (motion.y(epsilon) - motion.y(0)) / epsilon, 1.0E-5D);
        assertEquals(1.5, motion.x(1), 1.0E-12D);
        assertEquals(1, motion.y(1), 1.0E-12D);
    }

    @Test
    void teleportResetDoesNotDrawAcrossTheOldPath() {
        var motion = new ProjectileRenderMotion();
        motion.reset(0, 0, 0, 1, 0, 0);
        motion.advance(1, 0, 0);
        motion.reset(100, 200, -100, 0, 0, 0);
        for (double t : new double[] {0, 0.25, 0.5, 0.75, 1}) {
            assertEquals(100, motion.x(t));
            assertEquals(200, motion.y(t));
            assertEquals(-100, motion.z(t));
        }
    }
}
