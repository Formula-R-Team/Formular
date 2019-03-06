package io.github.formular_team.formular.math;

import java.util.List;

public interface Curve {
    boolean isStraight();

    Vector2 getPoint(final float t);

    Vector2 getPointAt(final float u);

    List<Vector2> getPoints(final int divisions);

    List<Vector2> getPoints();

    Vector2[] getSpacedPoints(final int divisions);

    Vector2[] getSpacedPoints();

    float getLength();

    float[] getLengths(final int divisions);

    default float[] getLengths() {
        return this.getLengths(200);
    }

    Vector2 getTangent(final float t);

    Vector2 getTangentAt(final float u);

    Curve copy();

    default FrenetFrameSet computeFrenetFrames(final int segments, final boolean closed) {
        // see http://www.cs.indiana.edu/pub/techreports/TR425.pdf
        final Vector3 normal = new Vector3();
        final Vector3[] tangents = new Vector3[1 + segments];
        final Vector3[] normals = new Vector3[1 + segments];
        final Vector3[] binormals = new Vector3[1 + segments];
        final Vector3 vec = new Vector3();
        final Matrix4 mat = new Matrix4();
        // compute the tangent vectors for each segment on the curve
        for (int i = 0; i <= segments; i++) {
            final float u = (float) i / segments;
            tangents[i] = new Vector3(this.getTangentAt(u));
            tangents[i].normalize();
        }
        // select an initial normal vector perpendicular to the first tangent vector,
        // and in the direction of the minimum tangent xyz component
        normals[0] = new Vector3();
        binormals[0] = new Vector3();
        float min = Float.POSITIVE_INFINITY;
        final float tx = Math.abs(tangents[0].x());
        final float ty = Math.abs(tangents[0].y());
        final float tz = Math.abs(tangents[0].z());
        if (tx <= min) {
            min = tx;
            normal.set(1.0F, 0.0F, 0.0F);
        }
        if (ty <= min) {
            min = ty;
            normal.set(0.0F, 1.0F, 0.0F);
        }
        if (tz <= min) {
            normal.set(0.0F, 0.0F, 1.0F);
        }
        vec.crossVectors(tangents[0], normal).normalize();
        normals[0].crossVectors(tangents[0], vec);
        binormals[0].crossVectors(tangents[0], normals[0]);
        // compute the slowly-varying normal and binormal vectors for each segment on the curve
        for (int i = 1; i <= segments; i++) {
            normals[i] = normals[i - 1].copy();
            binormals[i] = binormals[i - 1].copy();
            vec.crossVectors(tangents[i - 1], tangents[i]);
            if (vec.length() > Float.MIN_VALUE) {
                vec.normalize();
                final float theta = Mth.acos(Mth.clamp(tangents[i - 1].dot(tangents[i]), -1.0F, 1.0F));
                normals[i].apply(mat.makeRotationAxis(vec, theta));
            }
            binormals[i].crossVectors(tangents[i], normals[i]);
        }
        if (closed) {
            float theta = Mth.acos(Mth.clamp(normals[0].dot(normals[segments]), -1.0F, 1.0F));
            theta /= segments;
            if (tangents[0].dot(vec.crossVectors(normals[0], normals[segments])) > 0) {
                theta = -theta;
                for (int i = 1; i <= segments; i++) {
                    normals[i].apply(mat.makeRotationAxis(tangents[i], theta * i));
                    binormals[i].crossVectors(tangents[i], normals[i]);
                }
            }
        }
        return new FrenetFrameSet(tangents, normals, binormals);
    }

    final class FrenetFrameSet {
        private final Vector3[] tangents;
        private final Vector3[] normals;
        private final Vector3[] binormals;

        FrenetFrameSet(final Vector3[] tangents, final Vector3[] normals, final Vector3[] binormals) {
            this.tangents = tangents;
            this.normals = normals;
            this.binormals = binormals;
        }

        public Vector3[] getTangents() {
            return this.tangents;
        }

        public Vector3[] getNormals() {
            return this.normals;
        }

        public Vector3[] getBinormals() {
            return this.binormals;
        }
    }
}
