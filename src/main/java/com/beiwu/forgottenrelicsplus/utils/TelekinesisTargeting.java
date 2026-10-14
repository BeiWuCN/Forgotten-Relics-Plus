package com.beiwu.forgottenrelicsplus.utils;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TelekinesisTargeting {
    private static final int PROBE_COUNT = 31;
    private static final double VERTICAL_STEP = 0.5D;

    private TelekinesisTargeting() {}

    public static LivingEntity findTarget(Level level, Player player, Vec3 origin, Vec3 look, double range) {
        Vec3 probe = origin;
        for (int step = 1; step <= PROBE_COUNT; step++) {
            probe = probe.add(look.scale(step)).add(0.0D, VERTICAL_STEP, 0.0D);
            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class, boxAround(probe, range), candidate -> candidate != player && !candidate.isSpectator());
            if (!candidates.isEmpty()) {
                return candidates.getFirst();
            }
        }
        return null;
    }

    public static boolean isInReach(Vec3 origin, Vec3 look, AABB targetBounds, double range) {
        double x = origin.x;
        double y = origin.y;
        double z = origin.z;
        for (int step = 1; step <= PROBE_COUNT; step++) {
            x += look.x * step;
            y += look.y * step;
            y += VERTICAL_STEP;
            z += look.z * step;
            if (targetBounds.intersects(x - range, y - range, z - range, x + range, y + range, z + range)) {
                return true;
            }
        }
        return false;
    }

    private static AABB boxAround(Vec3 point, double range) {
        return new AABB(
                point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range);
    }
}
