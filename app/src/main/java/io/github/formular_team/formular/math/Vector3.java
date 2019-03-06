package io.github.formular_team.formular.math;

import java.util.Objects;

public final class Vector3 extends Vector2 {
    private float z;

    public Vector3() {
        this(0.0F, 0.0F, 0.0F);
    }

    public Vector3(final Vector2 vec) {
        this(vec.x(), 0.0F, vec.y());
    }

    public Vector3(final float x, final float y, final float z) {
        super(x, y);
        this.z = z;
    }

    public float z() {
        return this.z;
    }

    public Vector3 add(final Vector3 v) {
        super.add(v);
        this.z += v.z();
        return this;
    }

    @Override
    public Vector3 add(final float s) {
        super.add(s);
        this.z += s;
        return this;
    }

    public Vector3 add(final Vector3 a, final Vector3 b) {
        super.add(a, b);
        this.z = a.z() + b.z();
        return this;
    }

    public Vector3 applyAxisAngle(final Vector3 axis, final float angle) {
        final Quaternion quaternion = new Quaternion();
        quaternion.setFromAxisAngle(axis, angle);
        this.apply(quaternion);
        return this;
    }

    @Override
    public Vector3 apply(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();

        this.x = e[0] * x + e[3] * y + e[6] * z;
        this.y = e[1] * x + e[4] * y + e[7] * z;
        this.z = e[2] * x + e[5] * y + e[8] * z;
        return this;
    }

    public Vector3 apply(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();
        final float w = 1.0F / (e[3] * x + e[7] * y + e[11] * z + e[15]);
        this.x = (e[0] * x + e[4] * y + e[8] * z + e[12]) * w;
        this.y = (e[1] * x + e[5] * y + e[9] * z + e[13]) * w;
        this.z = (e[2] * x + e[6] * y + e[10] * z + e[14]) * w;
        return this;
    }

    public Vector3 apply(final Quaternion q) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float qx = q.x();
        final float qy = q.y();
        final float qz = q.z();
        final float qw = q.w();

        // calculate quat * vector

        final float ix = qw * x + qy * z - qz * y;
        final float iy = qw * y + qz * x - qx * z;
        final float iz = qw * z + qx * y - qy * x;
        final float iw = -qx * x - qy * y - qz * z;

        // calculate result * inverse quat

        this.x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
        this.y = iy * qw + iw * -qy + iz * -qx - ix * -qz;
        this.z = iz * qw + iw * -qz + ix * -qy - iy * -qx;
        return this;
    }

    public float angleTo(final Vector3 v) {
        final float theta = this.dot(v) / (Mth.sqrt(this.lengthSq() * v.lengthSq()));

        // clamp, to handle numerical problems

        return Mth.acos(Mth.clamp(theta, -1.0f, 1.0f));
    }

    @Override
    public Vector3 ceil() {
        super.ceil();
        this.z = Mth.ceil(this.z);
        return this;
    }

    public Vector3 clamp(final Vector3 min, final Vector3 max) {
        super.clamp(min, max);
        this.z = Math.max(min.z(), Math.min(max.z(), this.z));
        return this;
    }

    @Override
    public Vector3 clamp(final float min, final float max) {
        final float length = this.length();
        return this.divide(length).multiply(Math.max(min, Math.min(max, length)));
    }

    @Override
    public Vector3 clampScalar(final float minVal, final float maxVal) {
        final Vector3 min = new Vector3();
        final Vector3 max = new Vector3();
        min.set(minVal, minVal, minVal);
        max.set(maxVal, maxVal, maxVal);
        return this.clamp(min, max);
    }

    @Override
    public Vector3 copy() {
        return new Vector3(this.x, this.y, this.z);
    }

    public Vector3 copy(final Vector3 v) {
        super.copy(v);
        this.z = v.z();
        return this;
    }

    public Vector3 cross(final Vector3 v) {
        this.crossVectors(this, v);
        return this;
    }

    public Vector3 crossVectors(final Vector3 a, final Vector3 b) {
        final float ax = a.x();
        final float ay = a.y();
        final float az = a.z();
        final float bx = b.x();
        final float by = b.y();
        final float bz = b.z();
        this.x = ay * bz - az * by;
        this.y = az * bx - ax * bz;
        this.z = ax * by - ay * bx;
        return this;
    }

    public float distanceTo(final Vector3 v) {
        return Mth.sqrt(this.distanceToSquared(v));
    }

    public float manhattanDistanceTo(final Vector3 v) {
        return super.manhattanDistanceTo(v) + Math.abs(this.z - v.z());
    }

    public float distanceToSquared(final Vector3 v) {
        final float dz = this.z - v.z();
        return super.distanceToSquared(v) + dz * dz;
    }

    public Vector3 divide(final Vector3 v) {
        super.divide(v);
        this.z /= v.z();
        return this;
    }

    @Override
    public Vector3 divide(final float s) {
        return this.multiply(1.0F / s);
    }

    public float dot(final Vector3 v) {
        return super.dot(v) + this.z * v.z();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Vector3) {
            final Vector3 other = (Vector3) o;
            return Float.compare(other.x, this.x) == 0 && Float.compare(other.y, this.y) == 0 && Float.compare(other.z, this.z) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }

    @Override
    public Vector3 floor() {
        super.floor();
        this.z = Mth.floor(this.z);
        return this;
    }

    @Override
    public Vector2 fromArray(final float[] array) {
        return this.fromArray(array, 0);
    }

    @Override
    public Vector3 fromArray(final float[] array, final int offset) {
        super.fromArray(array, offset);
        this.z = array[offset + 2];
        return this;
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
    public float manhattanLength() {
        return super.manhattanLength() + Math.abs(this.z);
    }

    @Override
    public float lengthSq() {
        return super.lengthSq() + this.z * this.z;
    }

    public void lerp(final Vector3 v, final float alpha) {
        super.lerp(v, alpha);
        this.z += (v.z() - this.z) * alpha;
    }

    public void lerp(final Vector3 v1, final Vector3 v2, final float alpha) {
        this.sub(v2, v1);
        this.multiply(alpha);
        this.add(v1);
    }

    public void max(final Vector3 v) {
        super.max(v);
        this.z = Math.max(this.z, v.z());
    }

    public void min(final Vector3 v) {
        super.min(v);
        this.z = Math.min(this.z, v.z());
    }

    public void multiply(final Vector3 v) {
        super.multiply(v);
        this.z *= v.z();
    }

    @Override
    public Vector3 multiply(final float scalar) {
        super.multiply(scalar);
        this.z *= scalar;
        return this;
    }

    public void multiply(final Vector3 a, final Vector3 b) {
        this.x = a.x() * b.x();
        this.y = a.y() * b.y();
        this.z = a.z() * b.z();
    }

    @Override
    public Vector3 negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    @Override
    public Vector3 normalize() {
        this.divide(this.length());
        return this;
    }

    public void projectOnPlane(final Vector3 planeNormal) {
        final Vector3 v1 = new Vector3();
        v1.copy(this);
        v1.projectOnVector(planeNormal);
        this.sub(v1);
    }

    public void projectOnVector(final Vector3 vector) {
        final float scalar = vector.dot(this) / vector.lengthSq();

        this.copy(vector);
        this.multiply(scalar);
    }

    public void reflect(final Vector3 normal) {
        final Vector3 v1 = new Vector3();
        v1.copy(normal);
        v1.multiply(2.0F * this.dot(normal));
        this.sub(v1);
    }

    @Override
    public Vector3  round() {
        super.round();
        this.z = Math.round(this.z);
        return this;
    }

    @Override
    public Vector3 roundToZero() {
        super.roundToZero();
        if (this.z < 0.0F) {
            this.z = Mth.ceil(this.z);
        } else {
            this.z = Mth.floor(this.z);
        }
        return this;
    }

    public void set(final float x, final float y, final float z) {
        super.set(x, y);
        this.z = z;
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

    public void setFromCylindricalCoords(final float radius, final float theta, final float y) {
        this.x = radius * Mth.sin(theta);
        this.y = y;
        this.z = radius * Mth.cos(theta);
    }

    public void setFromMatrixColumn(final Matrix4 m, final int index) {
        this.fromArray(m.elements(), index * 4);
    }

    public void setFromMatrixPosition(final Matrix4 matrix) {
        final float[] e = matrix.elements();
        this.x = e[12];
        this.y = e[13];
        this.z = e[14];
    }

    public void setFromMatrixScale(final Matrix4 m) {
        this.setFromMatrixColumn(m, 0);
        final float sx = this.length();
        this.setFromMatrixColumn(m, 1);
        final float sy = this.length();
        this.setFromMatrixColumn(m, 2);
        final float sz = this.length();
        this.x = sx;
        this.y = sy;
        this.z = sz;
    }


    public void setFromSphericalCoords(final float radius, final float phi, final float theta) {
        final float sinPhiRadius = Mth.sin(phi) * radius;
        this.x = sinPhiRadius * Mth.sin(theta);
        this.y = Mth.cos(phi) * radius;
        this.z = sinPhiRadius * Mth.cos(theta);
    }

    @Override
    public Vector3 setLength(final float l) {
        this.normalize();
        return this.multiply(l);
    }

    @Override
    public Vector3 set(final float scalar) {
        super.set(scalar);
        this.z = scalar;
        return this;
    }

    @Override
    public Vector3 setX(final float x) {
        super.setX(x);
        return this;
    }

    @Override
    public Vector3 setY(final float y) {
        super.setY(y);
        return this;
    }

    public Vector3 setZ(final float z) {
        this.z = z;
        return this;
    }

    public Vector3 sub(final Vector3 v) {
        super.sub(v);
        this.z -= v.z();
        return this;
    }

    @Override
    public Vector3 sub(final float s) {
        super.sub(s);
        this.z -= s;
        return this;
    }

    public void sub(final Vector3 a, final Vector3 b) {
        this.x = a.x() - b.x();
        this.y = a.y() - b.y();
        this.z = a.z() - b.z();
    }

    @Override
    public float[] toArray() {
        return this.toArray(new float[3], 0);
    }

    @Override
    public float[] toArray(final float[] array) {
        return this.toArray(array, 0);
    }

    @Override
    public float[] toArray(final float[] array, final int offset) {
        array[offset] = this.x;
        array[offset + 1] = this.y;
        array[offset + 2] = this.z;
        return array;
    }

    public Vector3 transformDirection(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();
        this.x = e[0] * x + e[4] * y + e[8] * z;
        this.y = e[1] * x + e[5] * y + e[9] * z;
        this.z = e[2] * x + e[6] * y + e[10] * z;
        return this.normalize();
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
