package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class SmoothHomingTest {
    @Test
    void oppositeTargetTurnsGraduallyWithoutInstantReversal() {
        Vec3 motion = new Vec3(0.5, 0, 0);
        Vec3 target = new Vec3(-10, 0, 0);
        for (int tick = 0; tick < 30; tick++) {
            Vec3 next = SmoothHoming.steer(motion, target, 0.5);
            assertEquals(0.5, next.length(), 1.0E-12D);
            assertTrue(motion.normalize().dot(next.normalize()) >= Math.cos(Math.toRadians(14)) - 1.0E-12D);
            motion = next;
        }
        assertTrue(motion.normalize().dot(target.normalize()) > 0.999D);
    }

    @Test
    void accelerationConvergesWithoutSpeedOscillation() {
        Vec3 motion = Vec3.ZERO;
        for (int tick = 0; tick < 100; tick++) {
            Vec3 next = SmoothHoming.steer(motion, new Vec3(10, 0, 0), 0.5);
            assertTrue(next.length() >= motion.length());
            assertTrue(next.length() <= 0.5D);
            motion = next;
        }
        assertEquals(0.5D, motion.length(), 1.0E-8D);
    }

    @Test
    void changingTargetNeverIntroducesSharpCorners() {
        Vec3 motion = new Vec3(0.3, 0, 0);
        for (int tick = 0; tick < 200; tick++) {
            Vec3 aim = new Vec3(Math.cos(tick * 0.7), Math.sin(tick * 0.3), Math.sin(tick * 0.7));
            Vec3 next = SmoothHoming.steer(motion, aim, 0.3);
            assertTrue(motion.normalize().dot(next.normalize()) >= Math.cos(Math.toRadians(14)) - 1.0E-12D);
            assertEquals(0.3D, next.length(), 1.0E-12D);
            motion = next;
        }
    }

    @Test
    void approachingTargetBrakesToReduceCircling() {
        Vec3 motion = new Vec3(0.5, 0, 0);
        Vec3 next = SmoothHoming.steer(motion, new Vec3(0.1, 0, 0), 0.5);
        assertTrue(next.length() < motion.length());
        assertTrue(next.length() > 0);
    }

    @Test
    void verticalReversalAndZeroDistanceRemainFinite() {
        Vec3 motion = SmoothHoming.steer(new Vec3(0, 0.5, 0), new Vec3(0, -10, 0), 0.5);
        assertTrue(Double.isFinite(motion.length()));
        assertEquals(motion.scale(0.8D), SmoothHoming.steer(motion, Vec3.ZERO, 0.5));
    }
}
