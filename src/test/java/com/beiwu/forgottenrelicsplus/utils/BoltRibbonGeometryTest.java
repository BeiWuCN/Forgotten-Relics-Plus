package com.beiwu.forgottenrelicsplus.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BoltRibbonGeometryTest {
    @Test
    void emittedFacesPointTowardsCameraFromBothSides() {
        for (float[] points : new float[][] {
                {0, 0, -5, 1, 0.3F, -5, 2, 0, -5},
                {0, 0, 5, 1, 0.3F, 5, 2, 0, 5},
                {-5, 0, 0, -5, 0.3F, 1, -5, 0, 2},
                {5, 0, 0, 5, 0.3F, 1, 5, 0, 2}}) {
            float[] edges = new float[points.length];
            BoltRibbonGeometry.updateEdges(points, edges, 0.075F, 1, 0, 0);
            for (int i = 0; i < points.length - 3; i += 3) {
                boolean reverse = BoltRibbonGeometry.reverseWinding(points, edges, i);
                double[] a = vertex(points, edges, i, -1);
                double[] b = vertex(points, edges, reverse ? i + 3 : i, reverse ? -1 : 1);
                double[] c = vertex(points, edges, i + 3, 1);
                double ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
                double vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
                double facing = (uy * vz - uz * vy) * a[0]
                        + (uz * vx - ux * vz) * a[1] + (ux * vy - uy * vx) * a[2];
                assertTrue(facing < 0, "Ribbon face must point towards the camera");
            }
        }
    }

    private static double[] vertex(float[] points, float[] sides, int i, int sign) {
        return new double[] {points[i] + sign * sides[i], points[i + 1] + sign * sides[i + 1],
                points[i + 2] + sign * sides[i + 2]};
    }

    @Test
    void bendsMaintainWidthWithOneSharedEdgePerJoint() {
        float[] points = {0, 0, -5, 1, 0.4F, -5, 2, -0.2F, -5, 3, 0, -5};
        float[] edges = new float[points.length];
        BoltRibbonGeometry.updateEdges(points, edges, 0.075F, 1, 0, 0);
        for (int i = 0; i < points.length; i += 3) {
            double width = Math.sqrt(edges[i] * edges[i] + edges[i + 1] * edges[i + 1] + edges[i + 2] * edges[i + 2]);
            assertEquals(0.075D, width, 1.0E-7D);
            double dx = points[Math.min(i + 3, points.length - 3)] - points[Math.max(i - 3, 0)];
            double dy = points[Math.min(i + 3, points.length - 3) + 1] - points[Math.max(i - 3, 0) + 1];
            assertEquals(0, dx * edges[i] + dy * edges[i + 1], 1.0E-7D);
        }
    }

    @Test
    void cameraAlignedAndCoincidentPointsHaveFiniteEdges() {
        float[] points = {0, 0, -5, 0, 0, -5, 0, 0, -6};
        float[] edges = new float[points.length];
        BoltRibbonGeometry.updateEdges(points, edges, 0.1F, 1, 0, 0);
        for (float edge : edges) assertTrue(Float.isFinite(edge));
        assertEquals(0.1F, edges[0]);
        assertEquals(0.1F, edges[3]);
        assertEquals(0.1F, edges[6]);
    }

    @Test
    void reversingSegmentsDoNotFlipRibbonSides() {
        float[] points = {0, 0, -5, 1, 0, -5, 0.1F, 0, -5, -1, 0, -5};
        float[] edges = new float[points.length];
        BoltRibbonGeometry.updateEdges(points, edges, 0.1F, 1, 0, 0);
        for (int i = 3; i < edges.length; i += 3) {
            assertTrue(edges[i] * edges[i - 3] + edges[i + 1] * edges[i - 2] + edges[i + 2] * edges[i - 1] >= 0);
        }
    }
}
