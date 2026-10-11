package com.beiwu.forgottenrelicsplus.utils;

import net.minecraft.world.phys.Vec3;

public final class SmoothHoming {
    private static final double COS_TURN = Math.cos(Math.toRadians(14.0D));
    private static final double SIN_TURN = Math.sin(Math.toRadians(14.0D));

    private SmoothHoming() {}

    public static Vec3 steer(Vec3 motion, Vec3 displacement, double desiredSpeed) {
        double distance = displacement.length();
        if (distance < 1.0E-6D) return motion.scale(0.8D);
        desiredSpeed = Math.min(desiredSpeed, Math.max(desiredSpeed * 0.2D, distance * 0.4D));
        Vec3 desired = displacement.scale(1.0D / distance);
        double speed = motion.length();
        if (speed < 1.0E-6D) return desired.scale(desiredSpeed * 0.2D);
        Vec3 direction = motion.scale(1.0D / speed);
        double dot = Math.max(-1.0D, Math.min(1.0D, direction.dot(desired)));
        Vec3 nextDirection = desired;
        if (dot < COS_TURN) {
            Vec3 tangent = desired.subtract(direction.scale(dot));
            if (tangent.lengthSqr() < 1.0E-12D) {
                tangent = direction.cross(Math.abs(direction.y) < 0.9D
                        ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0));
            }
            nextDirection = direction.scale(COS_TURN).add(tangent.normalize().scale(SIN_TURN));
        }
        return nextDirection.scale(speed + (desiredSpeed - speed) * 0.2D);
    }
}
