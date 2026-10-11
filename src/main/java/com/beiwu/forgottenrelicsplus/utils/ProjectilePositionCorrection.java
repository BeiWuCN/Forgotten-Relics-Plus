package com.beiwu.forgottenrelicsplus.utils;

import net.minecraft.world.phys.Vec3;

public final class ProjectilePositionCorrection {
    private static final double FREQUENCY = 0.75D;
    private static final double DECAY = Math.exp(-FREQUENCY);
    private double remainingX;
    private double remainingY;
    private double remainingZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public boolean retarget(Vec3 correction) {
        return retarget(correction.x, correction.y, correction.z);
    }

    public boolean retarget(double x, double y, double z) {
        double distance = x * x + y * y + z * z;
        if (!Double.isFinite(distance) || distance > 256.0D) {
            reset();
            return false;
        }
        remainingX = x;
        remainingY = y;
        remainingZ = z;
        return true;
    }

    public Vec3 consume() {
        double error = remainingX * remainingX + remainingY * remainingY + remainingZ * remainingZ;
        double speed = velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ;
        if (error + speed < 1.0E-12D) {
            reset();
            return Vec3.ZERO;
        }
        double springX = velocityX - FREQUENCY * remainingX;
        double springY = velocityY - FREQUENCY * remainingY;
        double springZ = velocityZ - FREQUENCY * remainingZ;
        double nextX = (remainingX - springX) * DECAY;
        double nextY = (remainingY - springY) * DECAY;
        double nextZ = (remainingZ - springZ) * DECAY;
        Vec3 step = new Vec3(remainingX - nextX, remainingY - nextY, remainingZ - nextZ);
        velocityX = (velocityX - FREQUENCY * springX) * DECAY;
        velocityY = (velocityY - FREQUENCY * springY) * DECAY;
        velocityZ = (velocityZ - FREQUENCY * springZ) * DECAY;
        remainingX = nextX;
        remainingY = nextY;
        remainingZ = nextZ;
        return step;
    }

    public void reset() {
        remainingX = 0;
        remainingY = 0;
        remainingZ = 0;
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
    }
}
