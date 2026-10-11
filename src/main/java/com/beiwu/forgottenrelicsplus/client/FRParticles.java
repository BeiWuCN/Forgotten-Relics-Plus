package com.beiwu.forgottenrelicsplus.client;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

public final class FRParticles {

    private FRParticles() {
    }

    public static void sparkle(Level level, double x, double y, double z,
                               float r, float g, float b, float size, int m) {
        level.addParticle(SparkleParticleData.sparkle(size, r, g, b, m), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void wisp(Level level, double x, double y, double z,
                            float r, float g, float b, float size,
                            double xm, double ym, double zm) {
        wisp(level, x, y, z, r, g, b, size, xm, ym, zm, 1.0F);
    }

    public static void wisp(Level level, double x, double y, double z,
                            float r, float g, float b, float size,
                            double xm, double ym, double zm, float maxAgeMul) {
        level.addParticle(WispParticleData.wisp(size, r, g, b, maxAgeMul), x, y, z, xm, ym, zm);
    }

    public static void serverSparkle(ServerLevel level, double x, double y, double z,
                                     float r, float g, float b, float size, int m) {
        level.sendParticles(SparkleParticleData.sparkle(size, r, g, b, m), x, y, z, 0, 0.0D, 0.0D, 0.0D, 1.0D);
    }

    public static void serverWisp(ServerLevel level, double x, double y, double z,
                                  float r, float g, float b, float size,
                                  double xm, double ym, double zm) {
        serverWisp(level, x, y, z, r, g, b, size, xm, ym, zm, 1.0F);
    }

    public static void serverWisp(ServerLevel level, double x, double y, double z,
                                  float r, float g, float b, float size,
                                  double xm, double ym, double zm, float maxAgeMul) {
        level.sendParticles(WispParticleData.wisp(size, r, g, b, maxAgeMul), x, y, z, 0, xm, ym, zm, 1.0D);
    }

    public static void serverWispBurst(ServerLevel level, double x, double y, double z,
                                       float r, float g, float b, float size, float maxAgeMul,
                                       int count, double spread, double speed) {
        level.sendParticles(WispParticleData.wisp(size, r, g, b, maxAgeMul),
                x, y, z, count, spread, spread, spread, speed);
    }
}
