package io.github.formular_team.formular.math;

import java.util.List;

public final class Sphere {
    private final Vector3 center;

    private float radius;

    public Sphere() {
        this(new Vector3(), 0.0F);
    }

    public Sphere(final Vector3 center, final float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Vector3 center() {
        return this.center;
    }

    public float radius() {
        return this.radius;
    }

    public Sphere setFromPoints(final List<Vector3> points, final Vector3 optionalCenter) {
        if (optionalCenter != null) {
            this.center.copy(optionalCenter);
        } else {
            new Box3().setFromPoints(points).center(this.center);
        }
        float maxRadiusSq = 0.0F;
        for (final Vector3 point : points) {
            maxRadiusSq = Math.max(maxRadiusSq, this.center.distanceToSquared(point));
        }
        this.radius = Mth.sqrt(maxRadiusSq);
        return this;
    }
}
