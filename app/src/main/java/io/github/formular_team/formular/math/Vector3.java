package io.github.formular_team.formular.math;

import java.util.Objects;

public final class Vector3 {
    private float x;

    private float y;

    private float z;

    public Vector3() {
        this(0.0F, 0.0F, 0.0F);
    }

    public Vector3(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void add(final Vector3 v) {
        this.x += v.x();
        this.y += v.y();
        this.z += v.z();
    }

    public void addScalar(final float s) {
        this.x += s;
        this.y += s;
        this.z += s;
    }

    public void addVectors(final Vector3 a, final Vector3 b) {
        this.x = a.x() + b.x();
        this.y = a.y() + b.y();
        this.z = a.z() + b.z();
    }

    public void applyAxisAngle(final Vector3 axis, final float angle) {
        final Quaternion quaternion = new Quaternion();
        quaternion.setFromAxisAngle(axis, angle);
        this.applyQuaternion(quaternion);
    }

    public void applyMatrix3(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();

        this.x = e[0] * x + e[3] * y + e[6] * z;
        this.y = e[1] * x + e[4] * y + e[7] * z;
        this.z = e[2] * x + e[5] * y + e[8] * z;
    }

    public void applyMatrix4(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();

        final float w = 1.0f / (e[3] * x + e[7] * y + e[11] * z + e[15]);

        this.x = (e[0] * x + e[4] * y + e[8] * z + e[12]) * w;
        this.y = (e[1] * x + e[5] * y + e[9] * z + e[13]) * w;
        this.z = (e[2] * x + e[6] * y + e[10] * z + e[14]) * w;
    }

    public void applyQuaternion(final Quaternion q) {
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
    }

    public float angleTo(final Vector3 v) {
        final float theta = (float) (this.dot(v) / (Math.sqrt(this.lengthSq() * v.lengthSq())));

        // clamp, to handle numerical problems

        return (float) Math.acos(clamp(theta, -1.0f, 1.0f));
    }

    public void ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        this.z = (float) Math.ceil(this.z);
    }

    public void clamp(final Vector3 min, final Vector3 max) {
        this.x = Math.max(min.x(), Math.min(max.x(), this.x));
        this.y = Math.max(min.y(), Math.min(max.y(), this.y));
        this.z = Math.max(min.z(), Math.min(max.z(), this.z));
    }

    public void clampLength(final float min, final float max) {
        final float length = this.length();

        this.divideScalar(length);
        this.multiplyScalar(Math.max(min, Math.min(max, length)));
    }

    public void clampScalar(final float minVal, final float maxVal) {
        final Vector3 min = new Vector3();
        final Vector3 max = new Vector3();

        min.set(minVal, minVal, minVal);
        max.set(maxVal, maxVal, maxVal);

        this.clamp(min, max);
    }

    public Vector3 copy() {
        return new Vector3(this.x, this.y, this.z);
    }

    public void copy(final Vector3 v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
    }

    public void cross(final Vector3 v) {
        this.crossVectors(this, v);
    }

    public void crossVectors(final Vector3 a, final Vector3 b) {
        final float ax = a.x();
        final float ay = a.y();
        final float az = a.z();
        final float bx = b.x();
        final float by = b.y();
        final float bz = b.z();

        this.x = ay * bz - az * by;
        this.y = az * bx - ax * bz;
        this.z = ax * by - ay * bx;
    }

    public float distanceTo(final Vector3 v) {
        return (float) Math.sqrt(this.distanceToSquared(v));
    }

    public float manhattanDistanceTo(final Vector3 v) {
        return Math.abs(this.x - v.x()) + Math.abs(this.y - v.y()) + Math.abs(this.z - v.z());
    }

    public float distanceToSquared(final Vector3 v) {
        final float dx = this.x - v.x();
        final float dy = this.y - v.y();
        final float dz = this.z - v.z();

        return dx * dx + dy * dy + dz * dz;
    }

    public void divide(final Vector3 v) {
        this.x /= v.x();
        this.y /= v.y();
        this.z /= v.z();
    }

    public void divideScalar(final float s) {
        this.multiplyScalar(1f / s);
    }

    public float dot(final Vector3 v) {
        return this.x * v.x() + this.y * v.y() + this.z * v.z();
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

    public void floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        this.z = (float) Math.floor(this.z);
    }

    public void fromArray(final float[] array) {
        this.fromArray(array, 0);
    }

    public void fromArray(final float[] array, final int offset) {
        this.x = array[offset];
        this.y = array[offset + 1];
        this.z = array[offset + 2];
    }

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

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float manhattanLength() {
        return Math.abs(this.x) + Math.abs(this.y) + Math.abs(this.z);
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public void lerp(final Vector3 v, final float alpha) {
        this.x += (v.x() - this.x) * alpha;
        this.y += (v.y() - this.y) * alpha;
        this.z += (v.z() - this.z) * alpha;
    }

    public void lerpVectors(final Vector3 v1, final Vector3 v2, final float alpha) {
        this.subVectors(v2, v1);
        this.multiplyScalar(alpha);
        this.add(v1);
    }

    public void max(final Vector3 v) {
        this.x = Math.max(this.x, v.x());
        this.y = Math.max(this.y, v.y());
        this.z = Math.max(this.z, v.z());
    }

    public void min(final Vector3 v) {
        this.x = Math.min(this.x, v.x());
        this.y = Math.min(this.y, v.y());
        this.z = Math.min(this.z, v.z());
    }

    public void multiply(final Vector3 v) {
        this.x *= v.x();
        this.y *= v.y();
        this.z *= v.z();
    }

    public void multiplyScalar(final float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public void multiplyVectors(final Vector3 a, final Vector3 b) {
        this.x = a.x() * b.x();
        this.y = a.y() * b.y();
        this.z = a.z() * b.z();
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    public void normalize() {
        this.divideScalar(this.length());
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
        this.multiplyScalar(scalar);
    }

    public void reflect(final Vector3 normal) {
        final Vector3 v1 = new Vector3();
        v1.copy(normal);
        v1.multiplyScalar(2 * this.dot(normal));
        this.sub(v1);
    }

    public void round() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        this.z = Math.round(this.z);
    }

    public void roundToZero() {

        if (this.x < 0.0f) {
            this.x = (float) Math.ceil(this.x);
        } else {
            this.x = (float) Math.floor(this.x);
        }
        if (this.y < 0.0f) {
            this.y = (float) Math.ceil(this.y);
        } else {
            this.y = (float) Math.floor(this.y);
        }
        if (this.z < 0.0f) {
            this.z = (float) Math.ceil(this.z);
        } else {
            this.z = (float) Math.floor(this.z);
        }
    }

    public void set(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

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
        this.x = (float) (radius * Math.sin(theta));
        this.y = y;
        this.z = (float) (radius * Math.cos(theta));
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
        final float sinPhiRadius = (float) Math.sin(phi) * radius;

        this.x = (float) (sinPhiRadius * Math.sin(theta));
        this.y = (float) (Math.cos(phi) * radius);
        this.z = (float) (sinPhiRadius * Math.cos(theta));
    }

    public void setLength(final float l) {
        this.normalize();
        this.multiplyScalar(l);
    }

    public void setScalar(final float scalar) {
        this.x = scalar;
        this.y = scalar;
        this.z = scalar;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public void setZ(final float z) {
        this.z = z;
    }

    public void sub(final Vector3 v) {
        this.x -= v.x();
        this.y -= v.y();
        this.z -= v.z();
    }

    public void subScalar(final float s) {
        this.x -= s;
        this.y -= s;
        this.z -= s;
    }

    public void subVectors(final Vector3 a, final Vector3 b) {
        this.x = a.x() - b.x();
        this.y = a.y() - b.y();
        this.z = a.z() - b.z();
    }

    public float[] toArray() {
        return this.toArray(new float[3], 0);
    }

    public float[] toArray(final float[] array) {
        return this.toArray(array, 0);
    }

    public float[] toArray(final float[] array, final int offset) {
        array[offset] = this.x;
        array[offset + 1] = this.y;
        array[offset + 2] = this.z;

        return array;
    }


    public void transformDirection(final Matrix4 m) {
        final float x = this.x;
        final float y = this.y;
        final float z = this.z;
        final float[] e = m.elements();

        this.x = e[0] * x + e[4] * y + e[8] * z;
        this.y = e[1] * x + e[5] * y + e[9] * z;
        this.z = e[2] * x + e[6] * y + e[10] * z;

        this.normalize();
    }

    private static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(max, value));
    }
}
