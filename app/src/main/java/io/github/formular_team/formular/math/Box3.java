package io.github.formular_team.formular.math;

import java.util.List;

public class Box3 {
    private final Vector3 min;

    private final Vector3 max;

    public Box3() {
        this(
            new Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            new Vector3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        );
    }

    public Vector3 min() {
        return this.min;
    }

    public Vector3 max() {
        return this.max;
    }

    public Box3(final Vector3 min, final Vector3 max) {
        this.min = min;
        this.max = max;
    }

    public Box3 setFromPoints(final List<Vector3> vertices) {
        this.min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        this.max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (final Vector3 v : vertices) {
            this.min.min(v);
            this.max.max(v);
        }
        return this;
    }

    public Vector3 center() {
        return this.center(new Vector3());
    }

    public Vector3 center(final Vector3 optionalTarget) {
        return optionalTarget.add(this.min, this.max).multiply(0.5F);
    }
}
