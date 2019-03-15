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
package io.github.formular_team.formular.math;

import java.util.Arrays;
import java.util.List;

public class Box2 {
    private Vector2 min;

    private Vector2 max;

    private static final Vector2 _v1 = new Vector2();

    public Box2() {
        this(new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
    }

    public Box2(final Vector2 min, final Vector2 max) {
        this.min = min;
        this.max = max;
    }

    public Vector2 getMin() {
        return this.min;
    }

    public Vector2 getMax() {
        return this.max;
    }

    public void setMin(final Vector2 min) {
        this.min = min;
    }

    public void setMax(final Vector2 max) {
        this.max = max;
    }

    public Box2 set(final Vector2 min, final Vector2 max) {
        this.min.copy(min);
        this.max.copy(max);
        return this;
    }

    public Box2 setFromPoints(final Vector2... points) {
        return this.setFromPoints(Arrays.asList(points));
    }

    public Box2 setFromPoints(final List<Vector2> points) {
        this.makeEmpty();
        for (final Vector2 point : points) {
            this.expandByPoint(point);
        }
        return this;
    }

    public Box2 setFromCenterAndSize(final Vector2 center, final Vector2 size) {
        final Vector2 halfSize = _v1.copy(size).multiply(0.5F);
        this.min.copy(center).sub(halfSize);
        this.max.copy(center).add(halfSize);
        return this;
    }

    public Box2 copy(final Box2 box) {
        this.min.copy(box.min);
        this.max.copy(box.max);
        return this;
    }

    public Box2 makeEmpty() {
        this.min.x = this.min.y = Float.POSITIVE_INFINITY;
        this.max.x = this.max.y = Float.NEGATIVE_INFINITY;
        return this;
    }

    public boolean isEmpty() {
        return (this.max.getX() < this.min.getX()) || (this.max.getY() < this.min.getY());
    }

    public Vector2 center() {
        return this.center(new Vector2());
    }

    public Vector2 center(final Vector2 optionalTarget) {
        return optionalTarget.add(this.min, this.max).multiply(0.5F);
    }

    public Vector2 size() {
        return this.size(new Vector2());
    }

    public Vector2 size(final Vector2 optionalTarget) {
        return optionalTarget.sub(this.max, this.min);
    }

    public Box2 expandByPoint(final Vector2 point) {
        this.min.min(point);
        this.max.max(point);
        return this;
    }

    public Box2 expandByVector(final Vector2 vector) {
        this.min.sub(vector);
        this.max.add(vector);

        return this;
    }

    public Box2 expandByScalar(final float scalar) {
        this.min.add(-scalar);
        this.max.add(scalar);
        return this;
    }

    public boolean isContainsPoint(final Vector2 point) {
        return !(point.x < this.min.x) && !(point.x > this.max.x) &&
            !(point.y < this.min.y) && !(point.y > this.max.y);
    }

    public boolean isContainsBox(final Box2 box) {
        return (this.min.x <= box.min.x) && (box.max.x <= this.max.x) &&
            (this.min.y <= box.min.y) && (box.max.y <= this.max.y);

    }

    public Vector2 getParameter(final Vector2 point) {
        return this.getParameter(point, new Vector2());
    }

    public Vector2 getParameter(final Vector2 point, final Vector2 optionalTarget) {
        return optionalTarget.set(
            (point.x - this.min.x) / (this.max.x - this.min.x),
            (point.y - this.min.y) / (this.max.y - this.min.y)
        );
    }

    public boolean isIntersectionBox(final Box2 box) {
        return !(box.max.x < this.min.x) && !(box.min.x > this.max.x) &&
            !(box.max.y < this.min.y) && !(box.min.y > this.max.y);
    }

    public Vector2 clampPoint(final Vector2 point) {
        return this.clampPoint(point, new Vector2());
    }

    public Vector2 clampPoint(final Vector2 point, final Vector2 optionalTarget) {
        return optionalTarget.copy(point).clamp(this.min, this.max);
    }

    public float distanceToPoint(final Vector2 point) {
        final Vector2 v1 = new Vector2();
        final Vector2 clampedPoint = v1.copy(point).clamp(this.min, this.max);
        return clampedPoint.sub(point).length();
    }

    public Box2 intersect(final Box2 box) {
        this.min.max(box.min);
        this.max.min(box.max);

        return this;
    }

    public Box2 union(final Box2 box) {
        this.min.min(box.min);
        this.max.max(box.max);

        return this;
    }

    public Box2 translate(final Vector2 offset) {
        this.min.add(offset);
        this.max.add(offset);
        return this;
    }

    public boolean equals(final Box2 box) {
        return box.min.equals(this.min) && box.max.equals(this.max);
    }

    @Override
    public Box2 clone() {
        return new Box2().copy(this);
    }

    @Override
    public String toString() {
        return "{min:" + this.min.toString() + ", max:" + this.max.toString() + "}";
    }
}
