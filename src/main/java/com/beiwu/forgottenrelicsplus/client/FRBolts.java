package com.beiwu.forgottenrelicsplus.client;

import com.beiwu.forgottenrelicsplus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import com.beiwu.forgottenrelicsplus.utils.BoltRibbonGeometry;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.joml.Vector3f;

public final class FRBolts {
    private FRBolts() {}

    public static void registerProvider(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FRParticleTypes.BOLT.get(), sprites ->
                (data, level, x, y, z, dx, dy, dz) -> createBolt(data, level, sprites));
    }

    private static Particle createBolt(FRBoltParticleData data, ClientLevel level, SpriteSet sprites) {
        double lengthSquared = data.from().distanceToSqr(data.to());
        if (!Double.isFinite(lengthSquared) || lengthSquared < 1.0E-6D || lengthSquared > 65536.0D) {
            return null;
        }
        return new BoltParticle(data, level, sprites);
    }

    private static final class BoltParticle extends TextureSheetParticle {
        private final float[][] paths;
        private final float width;
        private AABB bounds;
        private final float[][] renderPoints;
        private final float[][] edges;
        private final int fromEntityId;
        private final int toEntityId;
        private final Vec3 originalDelta;
        private Vec3 from;
        private Vec3 to;
        private final Vector3f cameraRight = new Vector3f();

        private BoltParticle(FRBoltParticleData data, ClientLevel level, SpriteSet sprites) {
            super(level, data.from().x, data.from().y, data.from().z);
            pickSprite(sprites);
            lifetime = 8;
            hasPhysics = false;
            width = Mth.clamp(data.width(), 0.005F, 0.2F);
            rCol = data.red();
            gCol = data.green();
            bCol = data.blue();
            from = data.from();
            to = data.to();
            fromEntityId = data.fromEntityId();
            toEntityId = data.toEntityId();
            originalDelta = to.subtract(from);
            bounds = new AABB(from, to).inflate(0.75D);
            double dx = data.to().x - data.from().x;
            double dy = data.to().y - data.from().y;
            double dz = data.to().z - data.from().z;
            int segments = Mth.clamp((int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) * 2), 4, 24);
            paths = new float[Mth.clamp(data.count(), 1, 4)][(segments + 1) * 3];
            renderPoints = new float[paths.length][(segments + 1) * 3];
            edges = new float[paths.length][(segments + 1) * 3];
            for (float[] path : paths) {
                for (int point = 0; point <= segments; point++) {
                    double t = point / (double) segments;
                    double scatter = point == 0 || point == segments ? 0 : 0.25D;
                    path[point * 3] = (float) (dx * t + (random.nextDouble() - 0.5D) * scatter);
                    path[point * 3 + 1] = (float) (dy * t + (random.nextDouble() - 0.5D) * scatter);
                    path[point * 3 + 2] = (float) (dz * t + (random.nextDouble() - 0.5D) * scatter);
                }
            }
        }

        @Override
        public void tick() {
            if (age++ >= lifetime) {
                remove();
                return;
            }
            from = endpoint(fromEntityId, from, 1);
            to = endpoint(toEntityId, to, 1);
            bounds = new AABB(from, to).inflate(1.0D);
        }

        @Override
        public AABB getRenderBoundingBox(float partialTick) {
            return bounds;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        private Vec3 endpoint(int entityId, Vec3 fallback, float partialTick) {
            Entity entity = entityId < 0 ? null : level.getEntity(entityId);
            if (entity == null || entity.isRemoved()) return fallback;
            return new Vec3(
                    Mth.lerp(partialTick, entity.xOld, entity.getX()),
                    Mth.lerp(partialTick, entity.yOld, entity.getY()) + entity.getBbHeight() * 0.5D,
                    Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        }

        @Override
        public void render(VertexConsumer consumer, Camera camera, float partialTick) {
            Vec3 start = endpoint(fromEntityId, from, partialTick);
            Vec3 end = endpoint(toEntityId, to, partialTick);
            double ox = start.x - camera.getPosition().x;
            double oy = start.y - camera.getPosition().y;
            double oz = start.z - camera.getPosition().z;
            double shiftX = end.x - start.x - originalDelta.x;
            double shiftY = end.y - start.y - originalDelta.y;
            double shiftZ = end.z - start.z - originalDelta.z;
            cameraRight.set(1, 0, 0).rotate(camera.rotation());
            float opacity = 0.8F * Mth.clamp((lifetime - age - partialTick) / lifetime, 0, 1);
            float u = Mth.lerp(0.5F, getU0(), getU1());
            for (int strand = 0; strand < paths.length; strand++) {
                float[] path = paths[strand];
                float[] points = renderPoints[strand];
                float[] sides = edges[strand];
                for (int i = 0; i < path.length; i += 3) {
                    double t = i / (double) (path.length - 3);
                    points[i] = (float) (ox + path[i] + shiftX * t);
                    points[i + 1] = (float) (oy + path[i + 1] + shiftY * t);
                    points[i + 2] = (float) (oz + path[i + 2] + shiftZ * t);
                }
                BoltRibbonGeometry.updateEdges(points, sides, width, cameraRight.x, cameraRight.y, cameraRight.z);
                for (int i = 0; i < points.length - 3; i += 3) {
                    ribbonVertex(consumer, points, sides, i, -1, u, getV0(), opacity);
                    if (BoltRibbonGeometry.reverseWinding(points, sides, i)) {
                        ribbonVertex(consumer, points, sides, i + 3, -1, u, getV0(), opacity);
                        ribbonVertex(consumer, points, sides, i + 3, 1, u, getV1(), opacity);
                        ribbonVertex(consumer, points, sides, i, 1, u, getV1(), opacity);
                    } else {
                        ribbonVertex(consumer, points, sides, i, 1, u, getV1(), opacity);
                        ribbonVertex(consumer, points, sides, i + 3, 1, u, getV1(), opacity);
                        ribbonVertex(consumer, points, sides, i + 3, -1, u, getV0(), opacity);
                    }
                }
            }
        }

        private void ribbonVertex(VertexConsumer consumer, float[] points, float[] sides, int i, int sign,
                                  float u, float v, float alpha) {
            vertex(consumer, points[i] + sign * sides[i], points[i + 1] + sign * sides[i + 1],
                    points[i + 2] + sign * sides[i + 2], u, v, alpha);
        }

        private void vertex(VertexConsumer consumer, double x, double y, double z, float u, float v, float alpha) {
            consumer.addVertex((float) x, (float) y, (float) z)
                    .setUv(u, v)
                    .setColor(rCol, gCol, bCol, alpha)
                    .setLight(LightTexture.FULL_BRIGHT);
        }
    }
}
