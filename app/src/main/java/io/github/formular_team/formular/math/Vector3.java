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

/**
 * This class is realization of (X, Y, Z) vector.
 * Where:
 * X - x coordinate of the vector.
 * Y - y coordinate of the vector.
 * Z - z coordinate of the vector.
 *
 * @author thothbot
 */
public class Vector3 extends Vector2 {
    /**
     * The Z-coordinate
     */
    protected float z;

    // Temporary variables
    private static final Quaternion _quaternion = new Quaternion();

    static Matrix4 _matrix = new Matrix4();

    private static final Vector3 _min = new Vector3();

    private static final Vector3 _max = new Vector3();

    private static final Vector3 _v1 = new Vector3();

    /**
     * This default constructor will initialize vector (0, 0, 0);
     */
    public Vector3() {
        this(0, 0, 0);
    }

    /**
     * This constructor will initialize vector (X, Y, Z) from the specified
     * X, Y, Z coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Vector3(final float x, final float y, final float z) {
        super(x, y);
        this.z = z;
    }

    /**
     * get Z coordinate from the vector
     *
     * @return a Z coordinate
     */
    public float getZ() {
        return this.z;
    }

    /**
     * This method will add specified value to Z coordinate of the vector.
     * In another words: z += value.
     *
     * @param z the Y coordinate
     */
    private void addZ(final float z) {
        this.z += z;
    }

    /**
     * This method sets Z coordinate of the vector.
     *
     * @param z the Z coordinate
     */
    public void setZ(final float z) {
        this.z = z;
    }

    /**
     * Set value of the vector to the specified (X, Y, Z) coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Vector3 set(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set value of the vector to the specified (A, A, A) coordinates.
     *
     * @param a the X, Y and Z coordinate
     */
    public Vector3 set(final float a) {
        this.x = a;
        this.y = a;
        this.z = a;
        return this;
    }

    @Override
    public void setComponent(final int index, final float value) {

        switch (index) {

        case 0:
            this.x = value;
            break;
        case 1:
            this.y = value;
            break;
        case 2:
            this.z = value;
            break;
        default:
            throw new Error("index is out of range: " + index);

        }

    }

    @Override
    public float getComponent(final int index) {

        switch (index) {

        case 0:
            return this.x;
        case 1:
            return this.y;
        case 2:
            return this.z;
        default:
            throw new Error("index is out of range: " + index);

        }

    }

    /**
     * Set value of the vector from another vector.
     *
     * @param v the other vector
     * @return the current vector
     */
    public Vector3 copy(final Vector3 v) {
        this.set(v.getX(), v.getY(), v.getZ());

        return this;
    }

    public Vector3 add(final Vector3 v) {
        return this.add(this, v);
    }

    public Vector3 add(final Vector3 v1, final Vector3 v2) {
        this.x = v1.x + v2.x;
        this.y = v1.y + v2.y;
        this.z = v1.z + v2.z;

        return this;
    }

    @Override
    public Vector3 add(final float s) {
        this.addX(s);
        this.addY(s);
        this.addZ(s);

        return this;
    }

    public Vector3 sub(final Vector3 v) {
        return this.sub(this, v);
    }

    public Vector3 sub(final Vector3 v1, final Vector3 v2) {
        this.x = v1.x - v2.x;
        this.y = v1.y - v2.y;
        this.z = v1.z - v2.z;

        return this;
    }

    public Vector3 multiply(final Vector3 v) {
        return this.multiply(this, v);
    }

    public Vector3 multiply(final Vector3 v1, final Vector3 v2) {
        this.x = v1.x * v2.x;
        this.y = v1.y * v2.y;
        this.z = v1.z * v2.z;

        return this;
    }

    @Override
    public Vector3 multiply(final float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    public Vector3 applyAxisAngle(final Vector3 axis, final float angle) {
        this.apply(_quaternion.setFromAxisAngle(axis, angle));

        return this;
    }

    public Vector3 apply(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final Float32Array e = m.getArray();
        this.x = e.get(0) * x + e.get(3) * y + e.get(6) * z;
        this.y = e.get(1) * x + e.get(4) * y + e.get(7) * z;
        this.z = e.get(2) * x + e.get(5) * y + e.get(8) * z;
        return this;
    }

    public Vector3 apply(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final Float32Array e = m.getArray();
        this.x = e.get(0) * x + e.get(4) * y + e.get(8) * z + e.get(12);
        this.y = e.get(1) * x + e.get(5) * y + e.get(9) * z + e.get(13);
        this.z = e.get(2) * x + e.get(6) * y + e.get(10) * z + e.get(14);
        return this;
    }

    public Vector3 apply(final Quaternion q) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float qx = q.x;
        final float qy = q.y;
        final float qz = q.z;
        final float qw = q.w;
        final float ix = qw * x + qy * z - qz * y;
        final float iy = qw * y + qz * x - qx * z;
        final float iz = qw * z + qx * y - qy * x;
        final float iw = -qx * x - qy * y - qz * z;
        this.x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
        this.y = iy * qw + iw * -qy + iz * -qx - ix * -qz;
        this.z = iz * qw + iw * -qz + ix * -qy - iy * -qx;
        return this;
    }

    public Vector3 applyProjection(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final Float32Array e = m.getArray();
        final float d = 1.0f / (e.get(3) * x + e.get(7) * y + e.get(11) * z + e.get(15));
        this.x = (e.get(0) * x + e.get(4) * y + e.get(8) * z + e.get(12)) * d;
        this.y = (e.get(1) * x + e.get(5) * y + e.get(9) * z + e.get(13)) * d;
        this.z = (e.get(2) * x + e.get(6) * y + e.get(10) * z + e.get(14)) * d;
        return this;
    }

//    public Vector3 project(Camera camera)
//    {
//        _matrix.multiply( camera.getProjectionMatrix(), _matrix.getInverse( camera.getMatrixWorld() ) );
//        return this.applyProjection( _matrix );
//
//    }
//
//    public Vector3 unproject(Camera camera)
//    {
//        _matrix.multiply( camera.getMatrixWorld(), _matrix.getInverse( camera.getProjectionMatrix() ) );
//        return this.applyProjection( _matrix );
//    }

    /**
     * @param m Matrix4 affine matrix vector interpreted as a direction
     * @return
     */
    public Vector3 transformDirection(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;

        final Float32Array e = m.getArray();

        this.x = e.get(0) * x + e.get(4) * y + e.get(8) * z;
        this.y = e.get(1) * x + e.get(5) * y + e.get(9) * z;
        this.z = e.get(2) * x + e.get(6) * y + e.get(10) * z;

        this.normalize();

        return this;
    }

    public Vector3 divide(final Vector3 v) {
        return this.divide(this, v);
    }

    public Vector3 divide(final Vector3 v1, final Vector3 v2) {
        this.x = v1.x / v2.x;
        this.y = v1.y / v2.y;
        this.z = v1.z / v2.z;

        return this;
    }

    @Override
    public Vector3 divide(final float scalar) {
        if (scalar != 0) {

            final float invScalar = 1.0F / scalar;

            this.x *= invScalar;
            this.y *= invScalar;
            this.z *= invScalar;

        } else {

            this.x = 0;
            this.y = 0;
            this.z = 0;

        }

        return this;

    }

    public Vector3 min(final Vector3 v) {
        if (this.x > v.x) {
            this.x = v.x;
        }

        if (this.y > v.y) {
            this.y = v.y;
        }

        if (this.z > v.z) {
            this.z = v.z;
        }

        return this;
    }

    public Vector3 max(final Vector3 v) {
        if (this.x < v.x) {
            this.x = v.x;
        }

        if (this.y < v.y) {
            this.y = v.y;
        }

        if (this.z < v.z) {
            this.z = v.z;
        }

        return this;
    }

    /**
     * This function assumes min &#60; max, if this assumption isn't true it will not operate correctly
     */
    public Vector3 clamp(final Vector3 min, final Vector3 max) {
        // This function assumes min < max, if this assumption isn't true it will not operate correctly

        if (this.x < min.x) {

            this.x = min.x;

        } else if (this.x > max.x) {

            this.x = max.x;

        }

        if (this.y < min.y) {

            this.y = min.y;

        } else if (this.y > max.y) {

            this.y = max.y;

        }

        if (this.z < min.z) {

            this.z = min.z;

        } else if (this.z > max.z) {

            this.z = max.z;

        }

        return this;
    }

    @Override
    public Vector3 clamp(final float minVal, final float maxVal) {
        _min.set(minVal, minVal, minVal);
        _max.set(maxVal, maxVal, maxVal);

        return this.clamp(_min, _max);
    }

    @Override
    public Vector3 floor() {

        this.x = Mth.floor(this.x);
        this.y = Mth.floor(this.y);
        this.z = Mth.floor(this.z);

        return this;

    }

    @Override
    public Vector3 ceil() {

        this.x = Mth.ceil(this.x);
        this.y = Mth.ceil(this.y);
        this.z = Mth.ceil(this.z);

        return this;

    }

    @Override
    public Vector3 round() {

        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        this.z = Math.round(this.z);

        return this;

    }

    @Override
    public Vector3 roundToZero() {

        this.x = (this.x < 0) ? Mth.ceil(this.x) : Mth.floor(this.x);
        this.y = (this.y < 0) ? Mth.ceil(this.y) : Mth.floor(this.y);
        this.z = (this.z < 0) ? Mth.ceil(this.z) : Mth.floor(this.z);

        return this;

    }

    @Override
    public Vector3 negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;

        return this;
    }

    /**
     * Computes the dot product of this vector and vector v1.
     *
     * @param v1 the other vector
     * @return the dot product of this vector and v1
     */
    public float dot(final Vector3 v1) {
        return (this.x * v1.x + this.y * v1.y + this.z * v1.z);
    }

    /**
     * Returns the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    @Override
    public float lengthSq() {
        return this.dot(this);
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector
     */
    @Override
    public float length() {
        return Mth.sqrt(this.lengthSq());
    }

    public float lengthManhattan() {
        return Math.abs(this.x) + Math.abs(this.y) + Math.abs(this.z);
    }

    /**
     * Normalizes this vector in place.
     */
    @Override
    public Vector3 normalize() {
        return this.divide(this.length());
    }

    @Override
    public Vector3 setLength(final float l) {
        final float oldLength = this.length();

        if (oldLength != 0 && l != oldLength) {

            this.multiply(l / oldLength);
        }

        return this;
    }

    public Vector3 lerp(final Vector3 v1, final float alpha) {
        this.x += (v1.x - this.x) * alpha;
        this.y += (v1.y - this.y) * alpha;
        this.z += (v1.z - this.z) * alpha;

        return this;
    }

    /**
     * Sets this vector to be the vector cross product of vectors v1 and v2.
     *
     * @param a the first vector
     * @param b the second vector
     */
    public Vector3 cross(final Vector3 a, final Vector3 b) {
        final float ax = a.x;
        final float ay = a.y;
        final float az = a.z;
        final float bx = b.x;
        final float by = b.y;
        final float bz = b.z;

        this.x = ay * bz - az * by;
        this.y = az * bx - ax * bz;
        this.z = ax * by - ay * bx;

        return this;
    }

    public Vector3 cross(final Vector3 v) {
        return this.cross(this, v);
    }

    public Vector3 projectOnVector(final Vector3 vector) {
        _v1.copy(vector).normalize();

        final float dot = this.dot(_v1);

        return this.copy(_v1).multiply(dot);
    }

    public Vector3 projectOnPlane(final Vector3 planeNormal) {
        _v1.copy(this).projectOnVector(planeNormal);

        return this.sub(_v1);
    }


    /**
     * reflect incident vector off plane orthogonal to normal
     * normal is assumed to have unit length
     *
     * @param normal
     * @return
     */
    public Vector3 reflect(final Vector3 normal) {
        return this.sub(_v1.copy(normal).multiply(2 * this.dot(normal)));
    }

    public float angleTo(final Vector3 v) {
        final float theta = this.dot(v) / (this.length() * v.length());

        // clamp, to handle numerical problems

        return Mth.acos(Mth.clamp(theta, -1.0F, 1.0F));
    }

    public float distanceTo(final Vector3 v1) {
        return Mth.sqrt(this.distanceToSquared(v1));
    }

    public float distanceToSquared(final Vector3 v1) {
        final float dx = this.x - v1.x;
        final float dy = this.y - v1.y;
        final float dz = this.z - v1.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    public Vector3 setFromMatrixPosition(final Matrix4 m) {

        this.x = m.getArray().get(12);
        this.y = m.getArray().get(13);
        this.z = m.getArray().get(14);

        return this;
    }

    public Vector3 setFromMatrixScale(final Matrix4 m) {

        final Float32Array el = m.getArray();

        final float sx = this.set(el.get(0), el.get(1), el.get(2)).length();
        final float sy = this.set(el.get(4), el.get(5), el.get(6)).length();
        final float sz = this.set(el.get(8), el.get(9), el.get(10)).length();

        this.x = sx;
        this.y = sy;
        this.z = sz;

        return this;
    }

    public Vector3 setFromMatrixColumn(final int index, final Matrix4 matrix) {

        final int offset = index * 4;

        final Float32Array me = matrix.getArray();

        this.x = me.get(offset);
        this.y = me.get(offset + 1);
        this.z = me.get(offset + 2);

        return this;

    }

    /**
     * Returns true if all of the data members of v1 are equal to the
     * corresponding data members in this Vector3.
     *
     * @param v1 the vector with which the comparison is made
     * @return true or false
     */
    public boolean equals(final Vector3 v1) {
        return (this.x == v1.x && this.y == v1.y && this.z == v1.z);
    }

    @Override
    public Vector3 fromArray(final Float32Array array) {
        return this.fromArray(array, 0);
    }

    @Override
    public Vector3 fromArray(final Float32Array array, final int offset) {

        this.x = array.get(offset);
        this.y = array.get(offset + 1);
        this.z = array.get(offset + 2);

        return this;

    }

    @Override
    public Float32Array toArray() {
        return this.toArray(Float32Array.create(3), 0);
    }

    @Override
    public Float32Array toArray(final Float32Array array, final int offset) {

        array.set(offset, this.x);
        array.set(offset + 1, this.y);
        array.set(offset + 2, this.z);

        return array;
    }

    @Override
    public Vector3 clone() {
        return new Vector3(this.getX(), this.getY(), this.getZ());
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}