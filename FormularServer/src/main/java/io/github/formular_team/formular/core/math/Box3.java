package io.github.formular_team.formular.core.math;/*
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

import java.util.List;

public class Box3 {
    private Vector3 min;

    private Vector3 max;

    private static final Vector3 _v1 = new Vector3();

    public Box3() {
        this(new Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            new Vector3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
    }

    public Box3(final Vector3 min, final Vector3 max) {
        this.min = min;
        this.max = max;
    }

    public Vector3 getMin() {
        return this.min;
    }

    public void setMin(final Vector3 min) {
        this.min = min;
    }

    public Vector3 getMax() {
        return this.max;
    }

    public void setMax(final Vector3 max) {
        this.max = max;
    }

    public Box3 set(final Vector3 min, final Vector3 max) {
        this.min.copy(min);
        this.max.copy(max);
        return this;
    }

    public Box3 setFromPoints(final List<Vector3> points) {
        return this.setFromPoints(points.toArray(new Vector3[0]));
    }

    public Box3 setFromPoints(final Vector3[] points) {
        this.makeEmpty();
        for (final Vector3 point : points) {
            this.expandByPoint(point);
        }
        return this;
    }

    public Box3 setFromCenterAndSize(final Vector3 center, final Vector3 size) {
        final Vector3 halfSize = _v1.copy(size).multiply(0.5F);
        this.min.copy(center).sub(halfSize);
        this.max.copy(center).add(halfSize);
        return this;

    }

//    public Box3 setFromObject(final Object3D object) {
//        object.updateMatrixWorld(true);
//        this.makeEmpty();
//        object.traverse(new Object3D.Traverse() {
//            @Override
//            public void callback(final Object3D node) {
//                final AbstractGeometry geometry = ((GeometryObject) node).getGeometry();
//                final Vector3 v1 = new Vector3();
//                if (geometry != null) {
//                    if (geometry instanceof Geometry) {
//                        final List<Vector3> vertices = ((Geometry) geometry).getVertices();
//                        for (int i = 0, il = vertices.size(); i < il; i++) {
//                            v1.copy(vertices.get(i));
//                            v1.apply(node.getMatrixWorld());
//                            Box3.this.expandByPoint(v1);
//                        }
//                    } else if (geometry instanceof BufferGeometry && ((BufferGeometry) geometry).getAttribute("position") != null) {
//                        final Float32Array positions = (Float32Array) ((BufferGeometry) geometry).getAttribute("position").getArray();
//                        for (int i = 0, il = positions.getLength(); i < il; i += 3) {
//                            v1.set(positions.get(i), positions.get(i + 1), positions.get(i + 2));
//                            v1.apply(node.getMatrixWorld());
//                            Box3.this.expandByPoint(v1);
//                        }
//                    }
//                }
//            }
//        });
//        return this;
//    }

    public Box3 copy(final Box3 box) {
        this.min.copy(box.min);
        this.max.copy(box.max);
        return this;
    }

    public Box3 makeEmpty() {
        this.min.x = this.min.y = this.min.z = Float.POSITIVE_INFINITY;
        this.max.x = this.max.y = this.max.z = Float.NEGATIVE_INFINITY;
        return this;
    }

    public boolean isEmpty() {
        return (this.max.x < this.min.x) || (this.max.y < this.min.y) || (this.max.z < this.min.z);
    }

    public Vector3 center() {
        return this.center(new Vector3());
    }

    public Vector3 center(final Vector3 optionalTarget) {
        return optionalTarget.add(this.min, this.max).multiply(0.5F);
    }

    public Vector3 size() {
        return this.size(new Vector3());
    }

    public Vector3 size(final Vector3 optionalTarget) {
        return optionalTarget.sub(this.max, this.min);
    }

    public Box3 expandByPoint(final Vector3 point) {
        this.min.min(point);
        this.max.max(point);
        return this;
    }

    public Box3 expandByVector(final Vector3 vector) {
        this.min.sub(vector);
        this.max.add(vector);
        return this;
    }

    public Box3 expandByScalar(final float scalar) {
        this.min.add(-scalar);
        this.max.add(scalar);
        return this;
    }

    public boolean isContainsPoint(final Vector3 point) {
        return !(point.x < this.min.x) && !(point.x > this.max.x) &&
            !(point.y < this.min.y) && !(point.y > this.max.y) &&
            !(point.z < this.min.z) && !(point.z > this.max.z);
    }

    public boolean isContainsBox(final Box3 box) {
        return (this.min.x <= box.min.x) && (box.max.x <= this.max.x) &&
            (this.min.y <= box.min.y) && (box.max.y <= this.max.y) &&
            (this.min.z <= box.min.z) && (box.max.z <= this.max.z);
    }

    public Vector3 getParameter(final Vector3 point) {
        return new Vector3(
            (point.x - this.min.x) / (this.max.x - this.min.x),
            (point.y - this.min.y) / (this.max.y - this.min.y),
            (point.z - this.min.z) / (this.max.z - this.min.z)
        );
    }

    public boolean isIntersectionBox(final Box3 box) {
        return !(box.max.x < this.min.x) && !(box.min.x > this.max.x) &&
            !(box.max.y < this.min.y) && !(box.min.y > this.max.y) &&
            !(box.max.z < this.min.z) && !(box.min.z > this.max.z);
    }

    public Vector3 clampPoint(final Vector3 point) {
        return this.clampPoint(point, new Vector3());
    }

    public Vector3 clampPoint(final Vector3 point, final Vector3 optionalTarget) {
        return optionalTarget.copy(point).clamp(this.min, this.max);
    }

    public float distanceToPoint(final Vector3 point) {
        final Vector3 clampedPoint = _v1.copy(point).clamp(this.min, this.max);
        return clampedPoint.sub(point).length();
    }

    public Sphere getBoundingSphere() {
        return this.getBoundingSphere(new Sphere());
    }

    public Sphere getBoundingSphere(final Sphere optionalTarget) {
        optionalTarget.setCenter(this.center());
        optionalTarget.setRadius(this.size(_v1).length() * 0.5F);
        return optionalTarget;
    }

    public Box3 intersect(final Box3 box) {
        this.min.max(box.min);
        this.max.min(box.max);
        return this;
    }

    public Box3 union(final Box3 box) {
        this.min.min(box.min);
        this.max.max(box.max);
        return this;
    }

    public Box3 apply(final Matrix4 matrix) {
        final Vector3[] points = {
            new Vector3().set(this.min.x, this.min.y, this.min.z).apply(matrix),
            new Vector3().set(this.min.x, this.min.y, this.max.z).apply(matrix),
            new Vector3().set(this.min.x, this.max.y, this.min.z).apply(matrix),
            new Vector3().set(this.min.x, this.max.y, this.max.z).apply(matrix),
            new Vector3().set(this.max.x, this.min.y, this.min.z).apply(matrix),
            new Vector3().set(this.max.x, this.min.y, this.max.z).apply(matrix),
            new Vector3().set(this.max.x, this.max.y, this.min.z).apply(matrix),
            new Vector3().set(this.max.x, this.max.y, this.max.z).apply(matrix)
        };
        this.makeEmpty();
        this.setFromPoints(points);
        return this;
    }

    public Box3 translate(final Vector3 offset) {
        this.min.add(offset);
        this.max.add(offset);
        return this;
    }

    public boolean equals(final Box3 box) {
        return box.min.equals(this.min) && box.max.equals(this.max);
    }

    @Override
    public Box3 clone() {
        return new Box3().copy(this);
    }

    public String toString() {
        return "{min:" + this.min.toString() + ", max:" + this.max.toString() + "}";
    }
}
