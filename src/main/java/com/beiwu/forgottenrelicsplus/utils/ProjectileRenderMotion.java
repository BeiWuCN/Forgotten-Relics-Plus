package com.beiwu.forgottenrelicsplus.utils;

public final class ProjectileRenderMotion {
    private double previousX;
    private double previousY;
    private double previousZ;
    private double currentX;
    private double currentY;
    private double currentZ;
    private double previousVelocityX;
    private double previousVelocityY;
    private double previousVelocityZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public void reset(double x, double y, double z, double dx, double dy, double dz) {
        previousX = currentX = x;
        previousY = currentY = y;
        previousZ = currentZ = z;
        previousVelocityX = velocityX = dx;
        previousVelocityY = velocityY = dy;
        previousVelocityZ = velocityZ = dz;
    }

    public void advance(double x, double y, double z) {
        previousX = currentX;
        previousY = currentY;
        previousZ = currentZ;
        previousVelocityX = velocityX;
        previousVelocityY = velocityY;
        previousVelocityZ = velocityZ;
        velocityX = x - currentX;
        velocityY = y - currentY;
        velocityZ = z - currentZ;
        currentX = x;
        currentY = y;
        currentZ = z;
    }

    public double x(double fraction) {
        return interpolate(previousX, currentX, previousVelocityX, velocityX, fraction);
    }

    public double y(double fraction) {
        return interpolate(previousY, currentY, previousVelocityY, velocityY, fraction);
    }

    public double z(double fraction) {
        return interpolate(previousZ, currentZ, previousVelocityZ, velocityZ, fraction);
    }

    private static double interpolate(double start, double end, double incoming, double outgoing, double t) {
        t = Math.max(0.0D, Math.min(1.0D, t));
        double displacement = end - start;
        return start + t * (incoming + t * (3.0D * displacement - 2.0D * incoming - outgoing
                + t * (incoming + outgoing - 2.0D * displacement)));
    }
}
