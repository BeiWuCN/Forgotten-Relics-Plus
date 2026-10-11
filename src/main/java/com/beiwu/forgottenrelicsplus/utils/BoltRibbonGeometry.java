package com.beiwu.forgottenrelicsplus.utils;

public final class BoltRibbonGeometry {
    private BoltRibbonGeometry() {}

    public static boolean reverseWinding(float[] points, float[] sides, int i) {
        double dx = points[i + 3] - points[i] + sides[i + 3];
        double dy = points[i + 4] - points[i + 1] + sides[i + 4];
        double dz = points[i + 5] - points[i + 2] + sides[i + 5];
        double nx = sides[i + 1] * dz - sides[i + 2] * dy;
        double ny = sides[i + 2] * dx - sides[i] * dz;
        double nz = sides[i] * dy - sides[i + 1] * dx;
        return nx * points[i] + ny * points[i + 1] + nz * points[i + 2] > 0.0D;
    }

    public static void updateEdges(float[] points, float[] edges, float width,
                                   float fallbackX, float fallbackY, float fallbackZ) {
        for (int i = 0; i < points.length; i += 3) {
            int previous = Math.max(0, i - 3);
            int next = Math.min(points.length - 3, i + 3);
            double dx = points[next] - points[previous];
            double dy = points[next + 1] - points[previous + 1];
            double dz = points[next + 2] - points[previous + 2];
            double sx = dy * points[i + 2] - dz * points[i + 1];
            double sy = dz * points[i] - dx * points[i + 2];
            double sz = dx * points[i + 1] - dy * points[i];
            double squared = sx * sx + sy * sy + sz * sz;
            if (squared < 1.0E-12D) {
                sx = fallbackX;
                sy = fallbackY;
                sz = fallbackZ;
                squared = 1;
            }
            double scale = width / Math.sqrt(squared);
            if (i > 0 && sx * edges[i - 3] + sy * edges[i - 2] + sz * edges[i - 1] < 0) {
                scale = -scale;
            }
            edges[i] = (float) (sx * scale);
            edges[i + 1] = (float) (sy * scale);
            edges[i + 2] = (float) (sz * scale);
        }
    }
}
