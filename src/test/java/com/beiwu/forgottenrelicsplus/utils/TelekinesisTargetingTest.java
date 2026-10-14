package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TelekinesisTargetingTest {
    @Test
    void reachMatchesLegacySamplingAcrossDirectionsAndSizes() {
        Random random = new Random(1729);
        for (int i = 0; i < 2000; i++) {
            Vec3 origin = new Vec3(random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100);
            Vec3 look = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
            Vec3 probe = origin;
            int sample = 1 + random.nextInt(31);
            for (int step = 1; step <= sample; step++) {
                probe = probe.add(look.scale(step)).add(0, 0.5, 0);
            }
            Vec3 offset = new Vec3(random.nextDouble() * 20 - 10, random.nextDouble() * 20 - 10, random.nextDouble() * 20 - 10);
            Vec3 position = probe.add(offset);
            AABB bounds = new AABB(position, position.add(0.6, 1.8, 0.6));
            double range = i % 2 == 0 ? 3 : 6;
            assertEquals(legacyReach(origin, look, bounds, range), TelekinesisTargeting.isInReach(origin, look, bounds, range));
        }
    }

    @Test
    void usesBoundingBoxIntersectionRatherThanTargetCenter() {
        assertTrue(TelekinesisTargeting.isInReach(Vec3.ZERO, new Vec3(0, 0, 1), new AABB(2.9, 0, 0, 10, 2, 1), 3));
        assertFalse(TelekinesisTargeting.isInReach(Vec3.ZERO, new Vec3(0, 0, 1), new AABB(3, 0, 0, 10, 2, 1), 3));
    }

    @Test
    void finalProbeIsIncludedAndBeyondItIsExcluded() {
        Vec3 look = new Vec3(0, 0, 1);
        assertTrue(TelekinesisTargeting.isInReach(Vec3.ZERO, look, new AABB(-0.3, 15, 495, 0.3, 17, 497), 3));
        assertFalse(TelekinesisTargeting.isInReach(Vec3.ZERO, look, new AABB(-0.3, 15, 499, 0.3, 17, 501), 3));
    }

    private static boolean legacyReach(Vec3 origin, Vec3 look, AABB target, double range) {
        Vec3 probe = origin;
        for (int step = 1; step < 32; step++) {
            probe = probe.add(look.scale(step)).add(0, 0.5, 0);
            AABB box = new AABB(probe.x - range, probe.y - range, probe.z - range, probe.x + range, probe.y + range, probe.z + range);
            if (box.intersects(target)) {
                return true;
            }
        }
        return false;
    }
}
