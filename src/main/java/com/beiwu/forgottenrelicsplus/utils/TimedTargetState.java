package com.beiwu.forgottenrelicsplus.utils;

public final class TimedTargetState {
    public int ticksTillExpire;
    public int ticksCooldown;
    public int target = -1;
    public double dist = -1.0D;
    public double reDist = -1.0D;

    public void tick() {
        if (ticksCooldown > 0) {
            ticksCooldown--;
        }
        if (ticksTillExpire > 0) {
            ticksTillExpire--;
        }
        if (ticksTillExpire == 0) {
            clearTarget();
        }
    }

    public void clearTarget() {
        ticksTillExpire = 0;
        target = -1;
        dist = -1.0D;
        reDist = -1.0D;
    }

    public boolean isInactive() {
        return target == -1 && ticksTillExpire == 0 && ticksCooldown == 0;
    }
}
