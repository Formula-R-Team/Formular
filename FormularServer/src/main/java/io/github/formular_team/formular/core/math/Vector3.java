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

public class Vector3 extends Vector2 {
    protected float z;

    private static final Quaternion _quaternion = new Quaternion();

    private static final Vector3 _min = new Vector3();

    private static final Vector3 _max = new Vector3();

    private static final Vector3 _v1 = new Vector3();

    public Vector3() {
        this(0.0F, 0.0F, 0.0F);
    }

    public Vector3(final Vector2 v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public Vector3(final float x, final float y, final float z) {
        super(x, y);
        this.z = z;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    private void addZ(final float z) {
        this.z += z;
    }

    public void setZ(final float z) {
        this.z = z;
    }

    public Vector3 set(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

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

    @Override
    public Vector3 copy(final Vector2 v) {
        this.set(v.getX(), v.getY(), v.getZ());
        return this;
    }

    @Override
    public Vector3 add(final Vector2 v) {
        return this.add(this, v);
    }

    @Override
    public Vector3 add(final Vector2 v1, final Vector2 v2) {
        super.add(v1, v2);
        this.z = v1.getZ() + v2.getZ();
        return this;
    }

    @Override
    public Vector3 add(final float s) {
        super.add(s);
        this.addZ(s);
        return this;
    }

    @Override
    public Vector3 sub(final Vector2 v) {
        return this.sub(this, v);
    }

    @Override
    public Vector3 sub(final Vector2 v1, final Vector2 v2) {
        super.sub(v1, v2);
        this.z = v1.getZ() - v2.getZ();
        return this;
    }

    @Override
    public Vector3 multiply(final Vector2 v) {
        return this.multiply(this, v);
    }

    @Override
    public Vector3 multiply(final Vector2 v1, final Vector2 v2) {
        super.multiply(v1, v2);
        this.z = v1.getZ() * v2.getZ();
        return this;
    }

    @Override
    public Vector3 multiply(final float s) {
        super.multiply(s);
        this.z *= s;
        return this;
    }

    public Vector3 applyAxisAngle(final Vector3 axis, final float angle) {
        this.apply(_quaternion.setFromAxisAngle(axis, angle));
        return this;
    }

    @Override
    public Vector3 apply(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.getArray();
        this.x = e[0] * x + e[3] * y + e[6] * z;
        this.y = e[1] * x + e[4] * y + e[7] * z;
        this.z = e[2] * x + e[5] * y + e[8] * z;
        return this;
    }

    public Vector3 apply(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.getArray();
        this.x = e[0] * x + e[4] * y + e[8] * z + e[12];
        this.y = e[1] * x + e[5] * y + e[9] * z + e[13];
        this.z = e[2] * x + e[6] * y + e[10] * z + e[14];
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
        final float[] e = m.getArray();
        final float d = 1.0f / (e[3] * x + e[7] * y + e[11] * z + e[15]);
        this.x = (e[0] * x + e[4] * y + e[8] * z + e[12]) * d;
        this.y = (e[1] * x + e[5] * y + e[9] * z + e[13]) * d;
        this.z = (e[2] * x + e[6] * y + e[10] * z + e[14]) * d;
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

    public Vector3 transformDirection(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.getArray();
        this.x = e[0] * x + e[4] * y + e[8] * z;
        this.y = e[1] * x + e[5] * y + e[9] * z;
        this.z = e[2] * x + e[6] * y + e[10] * z;
        this.normalize();
        return this;
    }

    @Override
    public Vector3 divide(final Vector2 v) {
        return this.divide(this, v);
    }

    @Override
    public Vector3 divide(final Vector2 v1, final Vector2 v2) {
        super.divide(v1, v2);
        this.z = v1.getZ() / v2.getZ();
        return this;
    }

    @Override
    public Vector3 divide(final float scalar) {
        super.divide(scalar);
        if (scalar != 0) {
            this.z *= 1.0F / scalar;
        } else {
            this.z = 0.0F;
        }
        return this;
    }

    @Override
    public Vector3 min(final Vector2 v) {
        super.min(v);
        if (this.z > v.getZ()) {
            this.z = v.getZ();
        }
        return this;
    }

    @Override
    public Vector3 max(final Vector2 v) {
        super.max(v);
        if (this.z < v.getZ()) {
            this.z = v.getZ();
        }
        return this;
    }


    @Override
    public Vector3 clamp(final Vector2 min, final Vector2 max) {
        super.clamp(min, max);
        if (this.z < min.getZ()) {
            this.z = min.getZ();
        } else if (this.z > max.getZ()) {
            this.z = max.getZ();
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
        super.floor();
        this.z = Mth.floor(this.z);
        return this;

    }

    @Override
    public Vector3 ceil() {
        super.ceil();
        this.z = Mth.ceil(this.z);
        return this;
    }

    @Override
    public Vector3 round() {
        super.round();
        this.z = Math.round(this.z);
        return this;
    }

    @Override
    public Vector3 roundToZero() {
        super.roundToZero();
        this.z = (this.z < 0) ? Mth.ceil(this.z) : Mth.floor(this.z);
        return this;

    }

    @Override
    public Vector3 negate() {
        super.negate();
        this.z = -this.z;
        return this;
    }

    @Override
    public float dot(final Vector2 v1) {
        return (this.x * v1.x + this.y * v1.y + this.z * v1.getZ());
    }

    @Override
    public float lengthSq() {
        return this.dot(this);
    }

    @Override
    public float length() {
        return Mth.sqrt(this.lengthSq());
    }

    public float lengthManhattan() {
        return Math.abs(this.x) + Math.abs(this.y) + Math.abs(this.z);
    }

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

    @Override
    public Vector3 lerp(final Vector2 v1, final float alpha) {
        super.lerp(v1, alpha);
        this.z += (v1.getZ() - this.z) * alpha;
        return this;
    }

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

    public Vector3 reflect(final Vector3 normal) {
        return this.sub(_v1.copy(normal).multiply(2 * this.dot(normal)));
    }

    public float angleTo(final Vector3 v) {
        final float theta = this.dot(v) / (this.length() * v.length());
        return Mth.acos(Mth.clamp(theta, -1.0F, 1.0F));
    }

    @Override
    public float distanceTo(final Vector2 v1) {
        return Mth.sqrt(this.distanceToSquared(v1));
    }

    @Override
    public float distanceToSquared(final Vector2 v1) {
        final float dx = this.x - v1.x;
        final float dy = this.y - v1.y;
        final float dz = this.z - v1.getZ();
        return (dx * dx + dy * dy + dz * dz);
    }

    public Vector3 setFromMatrixPosition(final Matrix4 m) {
        this.x = m.getArray()[12];
        this.y = m.getArray()[13];
        this.z = m.getArray()[14];
        return this;
    }

    public Vector3 setFromMatrixScale(final Matrix4 m) {
        final float[] el = m.getArray();
        final float sx = this.set(el[0], el[1], el[2]).length();
        final float sy = this.set(el[4], el[5], el[6]).length();
        final float sz = this.set(el[8], el[9], el[10]).length();
        this.x = sx;
        this.y = sy;
        this.z = sz;
        return this;
    }

    public Vector3 setFromMatrixColumn(final int index, final Matrix4 matrix) {
        final int offset = index * 4;
        final float[] me = matrix.getArray();
        this.x = me[offset];
        this.y = me[offset + 1];
        this.z = me[offset + 2];
        return this;
    }

    public Vector3 modulo(final float n) {
        this.x = Mth.mod(this.x, n);
        this.y = Mth.mod(this.y, n);
        this.z = Mth.mod(this.z, n);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector3) {
            final Vector3 v1 = (Vector3) obj;
            return (this.x == v1.x && this.y == v1.y && this.z == v1.z);
        }
        return false;
    }

    @Override
    public Vector3 fromArray(final float[] array) {
        return this.fromArray(array, 0);
    }

    @Override
    public Vector3 fromArray(final float[] array, final int offset) {
        this.x = array[offset];
        this.y = array[offset + 1];
        this.z = array[offset + 2];
        return this;
    }

    @Override
    public float[] toArray() {
        return this.toArray(new float[3], 0);
    }

    @Override
    public float[] toArray(final float[] array, final int offset) {
        array[offset] = this.x;
        array[offset + 1] = this.y;
        array[offset + 2] = this.z;
        return array;
    }

    public Vector3 copy() {
        return new Vector3(this.getX(), this.getY(), this.getZ());
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public static Vector3 yz(final Vector2 vector, final float x) {
        return new Vector3(x, vector.getX(), vector.getY());
    }

    public static Vector3 xz(final Vector2 vector, final float y) {
        return new Vector3(vector.getX(), y, vector.getY());
    }

    public static Vector3 xy(final Vector2 vector, final float z) {
        return new Vector3(vector.getX(), vector.getY(), z);
    }
}
