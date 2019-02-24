package io.github.formular_team.formular.math;

import java.util.Objects;
import java.util.Optional;

public final class Ray {
    private final Vector3 origin;

    private final Vector3 direction;

    public Ray() {
        this(new Vector3(), new Vector3());
    }

    public Ray(final Vector3 origin, final Vector3 direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Ray set(final Vector3 origin, final Vector3 direction) {
        this.origin.copy(origin);
        this.direction.copy(direction);
        return this;
    }

    public Ray copy() {
        return new Ray().copy(this);
    }

    public Ray copy(final Ray ray) {
        this.origin.copy(ray.origin);
        this.direction.copy(ray.direction);
        return this;
    }

    public Vector3 at(final float t, final Vector3 target) {
        return target.copy(this.direction).multiplyScalar(t).add(this.origin);
    }

    public Ray lookAt(final Vector3 v) {
        this.direction.copy(v).sub(this.origin).normalize();
        return this;
    }

    public Ray recast(final float t) {
        this.origin.copy(this.at(t, new Vector3()));
        return this;
    }

    public float distanceToPlane(final Plane plane) {
        final float denominator = plane.normal().dot(this.direction);
        if (denominator == 0.0F) {
            if (plane.distanceToPoint(this.origin) == 0.0F) {
                return 0.0F;
            }
            return Float.NaN;
        }
        final float t = -(this.origin.dot(plane.normal()) + plane.constant()) / denominator;
        return t >= 0.0F ? t : Float.NaN;
    }

    public Optional<Vector3> intersectPlane(final Plane plane, final Vector3 target) {
        final float t = this.distanceToPlane(plane);
        if (Float.isNaN(t)) {
            return Optional.empty();
        }
        return Optional.of(this.at(t, target));
    }


    public boolean intersectsPlane(final Plane plane) {
        final float distToPoint = plane.distanceToPoint(this.origin);
        if (distToPoint == 0.0F) {
            return true;
        }
        final float denominator = plane.normal().dot(this.direction);
        return denominator * distToPoint < 0.0F;
    }

    public Ray applyMatrix4(final Matrix4 matrix) {
        this.origin.applyMatrix4(matrix);
        this.direction.transformDirection(matrix);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Ray) {
            final Ray other = (Ray) o;
            return Objects.equals(this.origin, other.origin) && Objects.equals(this.direction, other.direction);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.origin, this.direction);
    }
}
