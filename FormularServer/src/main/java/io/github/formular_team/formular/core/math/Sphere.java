/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 *
 * This file is part of Parallax project.
 *
 * Parallax is free software: you can redistribute it and/or modify it
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 *
 * Parallax is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution
 * 3.0 Unported License. for more details.
 *
 * You should have received a copy of the the Creative Commons Attribution
 * 3.0 Unported License along with Parallax.
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */
package io.github.formular_team.formular.core.math;

import java.util.List;

public class Sphere {
    private Vector3 center;

    private float radius;

    private static final Box3 _box = new Box3();

    public Sphere() {
        this(new Vector3(), 0.0F);
    }

    public Sphere(final float radius) {
        this(new Vector3(), radius);
    }

    public Sphere(final Vector3 center, final float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Vector3 getCenter() {
        return this.center;
    }

    public void setCenter(final Vector3 center) {
        this.center = center;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
    }

    public Sphere set(final Vector3 center, final float radius) {
        this.center.copy(center);
        this.radius = radius;

        return this;
    }

    public Sphere setFromPoints(final List<Vector3> points, final Vector3 optionalCenter) {
        return this.setFromPoints(points.toArray(new Vector3[points.size()]), optionalCenter);
    }

    public Sphere setFromPoints(final Vector3[] points, final Vector3 optionalCenter) {
        if (optionalCenter != null) {
            this.center.copy(optionalCenter);
        } else {
            _box.setFromPoints(points).center(this.center);
        }
        float maxRadiusSq = 0.0F;
        for (final Vector3 point : points) {
            maxRadiusSq = Math.max(maxRadiusSq, this.center.distanceToSquared(point));
        }
        this.radius = Mth.sqrt(maxRadiusSq);
        return this;
    }

    public Sphere copy(final Sphere sphere) {
        this.center.copy(sphere.center);
        this.radius = sphere.radius;
        return this;
    }

    public boolean isEmpty() {
        return (this.radius <= 0);
    }

    public boolean isContainsPoint(final Vector3 point) {
        return (point.distanceToSquared(this.center) <= (this.radius * this.radius));
    }

    public float distanceToPoint(final Vector3 point) {
        return (point.distanceTo(this.center) - this.radius);
    }

    public boolean isIntersectsSphere(final Sphere sphere) {
        final float radiusSum = this.radius + sphere.radius;
        return sphere.center.distanceToSquared(this.center) <= (radiusSum * radiusSum);
    }

    public Vector3 clampPoint(final Vector3 point) {
        return this.clampPoint(point, new Vector3());
    }

    public Vector3 clampPoint(final Vector3 point, final Vector3 optionalTarget) {
        final float deltaLengthSq = this.center.distanceToSquared(point);
        optionalTarget.copy(point);
        if (deltaLengthSq > (this.radius * this.radius)) {
            optionalTarget.sub(this.center).normalize();
            optionalTarget.multiply(this.radius).add(this.center);
        }
        return optionalTarget;
    }

    public Box3 getBoundingBox() {
        return this.getBoundingBox(new Box3());
    }

    public Box3 getBoundingBox(final Box3 optionalTarget) {
        optionalTarget.set(this.center, this.center);
        optionalTarget.expandByScalar(this.radius);
        return optionalTarget;
    }

    public Sphere apply(final Matrix4 matrix) {
        this.center.apply(matrix);
        this.radius = this.radius * matrix.getMaxScaleOnAxis();
        return this;
    }

    public Sphere translate(final Vector3 offset) {
        this.center.add(offset);
        return this;
    }

    public boolean equals(final Sphere sphere) {
        return sphere.center.equals(this.center) && (sphere.radius == this.radius);
    }

    @Override
    public Sphere clone() {
        return new Sphere().copy(this);
    }
}
