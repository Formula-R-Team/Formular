package io.github.formular_team.formular.math;

import java.util.Objects;

public final class Plane {
    private final Vector3 normal;

    private float constant;

    public Plane() {
        this(new Vector3(), 0.0F);
    }

    public Plane(final Vector3 normal, final float constant) {
        this.normal = normal;
        this.constant = constant;
    }

    public Vector3 normal() {
        return this.normal;
    }

    public float constant() {
        return this.constant;
    }

    public Plane set(final Vector3 normal, final float constant) {
        this.normal.copy(normal);
        this.constant = constant;
        return this;
    }

    public Plane setFromNormalAndCoplanarPoint(final Vector3 normal, final Vector3 point) {
        this.normal.copy(normal);
        this.constant = -point.dot(this.normal);
        return this;
    }

    public Plane setFromCoplanarPoints(final Vector3 a, final Vector3 b, final Vector3 c) {
        this.setFromNormalAndCoplanarPoint(c.sub(b).cross(a.sub(b)).negate(), a);
        return this;
    }

    public Plane copy() {
        return new Plane().copy(this);
    }

    public Plane copy(final Plane plane) {
        this.normal.copy(plane.normal);
        this.constant = plane.constant;
        return this;
    }

    public Plane normalize() {
        final float invNormalLen = 1.0F / this.normal.length();
        this.normal.multiply(invNormalLen);
        this.constant *= invNormalLen;
        return this;
    }

    public Plane negate() {
        this.normal.negate();
        this.constant = -this.constant;
        return this;
    }

    public float distanceToPoint(final Vector3 point) {
        return this.normal.dot(point) + this.constant;
    }

    public Vector3 projectPoint(final Vector3 point, final Vector3 target) {
        return target.copy(this.normal).multiply(-this.distanceToPoint(point)).add(point);
    }

    public Vector3 coplanarPoint(final Vector3 target) {
        return target.copy(this.normal).multiply(-this.constant);
    }

    public Plane applyMatrix4(final Matrix4 matrix) {
        final Matrix3 normalMatrix = new Matrix3().getNormalMatrix(matrix);
        final Vector3 referencePoint = this.coplanarPoint(new Vector3()).apply(matrix);
        final Vector3 normal = this.normal.apply(normalMatrix).normalize();
        this.constant =-referencePoint.dot(normal);
        return this;
    }

    public Plane translate(final Vector3 offset) {
        this.constant -= offset.dot(this.normal);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Plane) {
            final Plane other = (Plane) o;
            return Float.compare(other.constant, this.constant) == 0 && Objects.equals(this.normal, other.normal);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.normal, this.constant);
    }
}
