package io.github.formular_team.formular.math;

import java.util.Objects;

public final class Vector2 {
    private float x;

    private float y;

    public Vector2() {
        this(0.0F, 0.0F);
    }

    public Vector2(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public float height() {
        return this.y;
    }

    public float y() {
        return this.y;
    }

    public float width() {
        return this.x;
    }

    public float x() {
        return this.x;
    }

    public void add(final Vector2 v) {
        this.x += v.width();
        this.y += v.height();
    }

    public void addScalar(final float s) {
        this.x += s;
        this.y += s;
    }

    public void addScaledVector(final Vector2 v, final float s) {
        this.x += v.width() * s;
        this.y += v.height() * s;
    }

    public void addVectors(final Vector2 a, final Vector2 b) {
        this.x = a.width() + b.width();
        this.y = a.height() + b.height();
    }

    public float angle() {
        float angle = (float) Math.atan2(this.y, this.x);

        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        return angle;
    }


    public void applyMatrix3(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;

        final float[] e = m.elements();

        this.x = e[0] * x + e[3] * y + e[6];
        this.y = e[1] * x + e[4] * y + e[7];
    }

    public void ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
    }

    public void clamp(final Vector2 min, final Vector2 max) {
        this.x = Math.max(min.width(), Math.min(max.width(), this.width()));
        this.y = Math.max(min.height(), Math.min(max.height(), this.height()));
    }

    public void clampLength(final float min, final float max) {

        final float length = this.length();

        this.divideScalar(length);
        this.multiplyScalar(Math.max(min, Math.min(max, length)));
    }

    public void clampScalar(final float min, final float max) {
        final Vector2 minV = new Vector2(min, min);
        final Vector2 maxV = new Vector2(max, max);

        this.clamp(minV, maxV);
    }

    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    public void copy(final Vector2 that) {
        this.x = that.width();
        this.y = that.height();
    }

    public float distanceTo(final Vector2 v) {

        return (float) Math.sqrt(this.distanceToSquared(v));
    }

    public float manhattanDistanceTo(final Vector2 v) {
        return Math.abs(this.x - v.width()) + Math.abs(this.y - v.height());
    }

    public float distanceToSquared(final Vector2 v) {
        final float dx = this.x - v.width();
        final float dy = this.y - v.height();
        return dx * dx + dy * dy;
    }

    public void divide(final Vector2 v) {

        if (v.width() != 0.0f) {
            this.x /= v.width();
        }
        if (v.height() != 0.0f) {
            this.y /= v.height();
        }
    }

    public void divideScalar(final float s) {
        this.multiplyScalar(1 / s);
    }

    public float dot(final Vector2 v) {
        return this.x * v.width() + this.y * v.height();
    }

    public float cross(final Vector2 v) {
        return this.x * v.height() - this.y * v.width();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Vector2) {
            final Vector2 other = (Vector2) o;
            return Float.compare(other.x, this.x) == 0 && Float.compare(other.y, this.y) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    public void floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
    }

    public void fromArray(final float[] array, final int offset) {
        this.x = array[offset];
        this.y = array[offset + 1];
    }

    public void fromArray(final float[] array) {
        this.x = array[0];
        this.y = array[1];
    }


    public float getComponent(final int index) {
        switch (index) {

        case 0:
            return this.x;
        case 1:
            return this.y;
        default:
            throw new Error("index is out of range: " + index);

        }
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public float manhattanLength() {
        return Math.abs(this.x) + Math.abs(this.y);
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y;
    }

    public void lerp(final Vector2 v, final float alpha) {
        this.x += (v.width() - this.x) * alpha;
        this.y += (v.height() - this.y) * alpha;
    }

    public void lerpVectors(final Vector2 v1, final Vector2 v2, final float alpha) {
        this.subVectors(v2, v1);
        this.multiplyScalar(alpha);
        this.add(v1);
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public void normalize() {
        this.divideScalar(this.length());
    }

    public void max(final Vector2 v) {
        if (this.x < v.width()) {
            this.x = v.width();
        }
        if (this.y < v.height()) {
            this.y = v.height();
        }
    }

    public void min(final Vector2 v) {
        if (this.x > v.width()) {
            this.x = v.width();
        }
        if (this.y > v.height()) {
            this.y = v.height();
        }
    }

    public void multiply(final Vector2 v) {
        this.x *= v.width();
        this.y *= v.height();
    }

    public void multiplyScalar(final float s) {
        this.x *= s;
        this.y *= s;
    }

    public void rotateAround(final Vector2 center, final float angle) {
        final float c = (float) Math.cos(angle);
        final float s = (float) Math.sin(angle);

        final float x = this.x - center.width();
        final float y = this.y - center.height();

        this.x = x * c - y * s + center.width();
        this.y = x * s + y * c + center.height();
    }

    public void round() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
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
    }

    public void set(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public void setComponent(final int index, final float value) {
        switch (index) {

        case 0:
            this.x = value;
            break;
        case 1:
            this.y = value;
            break;
        default:
            throw new Error("index is out of range: " + index);

        }
    }

    public void setLength(final float l) {
        this.normalize();
        this.multiplyScalar(l);
    }

    public void setScalar(final float scalar) {
        this.x = scalar;
        this.y = scalar;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public void sub(final Vector2 v) {
        this.x -= v.width();
        this.y -= v.height();
    }

    public void subScalar(final float s) {
        this.x -= s;
        this.y -= s;
    }

    public void subVectors(final Vector2 a, final Vector2 b) {
        this.x = a.width() - b.width();
        this.y = a.height() - b.height();
    }


    public float[] toArray() {
        return new float[] { this.x, this.y };
    }

    public float[] toArray(final float[] array) {
        return this.toArray(array, 0);
    }

    public float[] toArray(final float[] array, final int offset) {
        array[offset] = this.x;
        array[offset + 1] = this.y;
        return array;
    }
}
