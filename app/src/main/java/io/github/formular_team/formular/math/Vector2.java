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

    public Vector2 add(final Vector2 v) {
        this.x += v.width();
        this.y += v.height();
        return this;
    }

    public Vector2 addScalar(final float s) {
        this.x += s;
        this.y += s;
        return this;
    }

    public Vector2 addScaledVector(final Vector2 v, final float s) {
        this.x += v.width() * s;
        this.y += v.height() * s;
        return this;
    }

    public Vector2 addVectors(final Vector2 a, final Vector2 b) {
        this.x = a.width() + b.width();
        this.y = a.height() + b.height();
        return this;
    }

    public float angle() {
        final float angle = Mth.atan2(this.y, this.x);
        return angle < 0.0F ? angle + 2.0F * (float) Math.PI : angle;
    }


    public Vector2 applyMatrix3(final Matrix3 m) {
        final float x = this.x;
        final float y = this.y;
        final float[] e = m.elements();
        this.x = e[0] * x + e[3] * y + e[6];
        this.y = e[1] * x + e[4] * y + e[7];
        return this;
    }

    public Vector2 ceil() {
        this.x = Mth.ceil(this.x);
        this.y = Mth.ceil(this.y);
        return this;
    }

    public Vector2 clamp(final Vector2 min, final Vector2 max) {
        this.x = Math.max(min.width(), Math.min(max.width(), this.width()));
        this.y = Math.max(min.height(), Math.min(max.height(), this.height()));
        return this;
    }

    public Vector2 clampLength(final float min, final float max) {
        final float length = this.length();
        return this.divideScalar(length).multiplyScalar(Math.max(min, Math.min(max, length)));
    }

    public Vector2 clampScalar(final float min, final float max) {
        final Vector2 minV = new Vector2(min, min);
        final Vector2 maxV = new Vector2(max, max);
        return this.clamp(minV, maxV);
    }

    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    public Vector2 copy(final Vector2 that) {
        this.x = that.width();
        this.y = that.height();
        return this;
    }

    public float distanceTo(final Vector2 v) {
        return Mth.sqrt(this.distanceToSquared(v));
    }

    public float manhattanDistanceTo(final Vector2 v) {
        return Math.abs(this.x - v.width()) + Math.abs(this.y - v.height());
    }

    public float distanceToSquared(final Vector2 v) {
        final float dx = this.x - v.width();
        final float dy = this.y - v.height();
        return dx * dx + dy * dy;
    }

    public Vector2 divide(final Vector2 v) {
        if (v.width() != 0.0F) {
            this.x /= v.width();
        }
        if (v.height() != 0.0F) {
            this.y /= v.height();
        }
        return this;
    }

    public Vector2 divideScalar(final float s) {
        return this.multiplyScalar(1.0F / s);
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

    public Vector2 floor() {
        this.x = Mth.floor(this.x);
        this.y = Mth.floor(this.y);
        return this;
    }

    public Vector2 fromArray(final float[] array, final int offset) {
        this.x = array[offset];
        this.y = array[offset + 1];
        return this;
    }

    public Vector2 fromArray(final float[] array) {
        this.x = array[0];
        this.y = array[1];
        return this;
    }


    public float getComponent(final int index) {
        switch (index) {
        case 0:
            return this.x;
        case 1:
            return this.y;
        default:
            throw new IllegalArgumentException("index is out of range: " + index);
        }
    }

    public float length() {
        return Mth.sqrt(this.x * this.x + this.y * this.y);
    }

    public float manhattanLength() {
        return Math.abs(this.x) + Math.abs(this.y);
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y;
    }

    public Vector2 lerp(final Vector2 v, final float alpha) {
        this.x += (v.width() - this.x) * alpha;
        this.y += (v.height() - this.y) * alpha;
        return this;
    }

    public Vector2 lerpVectors(final Vector2 v1, final Vector2 v2, final float alpha) {
        return this.subVectors(v2, v1).multiplyScalar(alpha).add(v1);
    }

    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vector2 normalize() {
        return this.divideScalar(this.length());
    }

    public Vector2 max(final Vector2 v) {
        if (this.x < v.width()) {
            this.x = v.width();
        }
        if (this.y < v.height()) {
            this.y = v.height();
        }
        return this;
    }

    public Vector2 min(final Vector2 v) {
        if (this.x > v.width()) {
            this.x = v.width();
        }
        if (this.y > v.height()) {
            this.y = v.height();
        }
        return this;
    }

    public Vector2 multiply(final Vector2 v) {
        this.x *= v.width();
        this.y *= v.height();
        return this;
    }

    public Vector2 multiplyScalar(final float s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    public Vector2 rotateAround(final Vector2 center, final float angle) {
        final float c = Mth.cos(angle);
        final float s = Mth.sin(angle);
        final float x = this.x - center.width();
        final float y = this.y - center.height();
        this.x = x * c - y * s + center.width();
        this.y = x * s + y * c + center.height();
        return this;
    }

    public Vector2 round() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        return this;
    }

    public Vector2 roundToZero() {
        if (this.x < 0.0F) {
            this.x = Mth.ceil(this.x);
        } else {
            this.x = Mth.floor(this.x);
        }
        if (this.y < 0.0F) {
            this.y = Mth.ceil(this.y);
        } else {
            this.y = Mth.floor(this.y);
        }
        return this;
    }

    public Vector2 set(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
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
            throw new IllegalArgumentException("index is out of range: " + index);
        }
    }

    public Vector2 setLength(final float l) {
        return this.normalize().multiplyScalar(l);
    }

    public Vector2 setScalar(final float scalar) {
        this.x = scalar;
        this.y = scalar;
        return this;
    }

    public Vector2 setX(final float x) {
        this.x = x;
        return this;
    }

    public Vector2 setY(final float y) {
        this.y = y;
        return this;
    }

    public Vector2 sub(final Vector2 v) {
        this.x -= v.width();
        this.y -= v.height();
        return this;
    }

    public Vector2 subScalar(final float s) {
        this.x -= s;
        this.y -= s;
        return this;
    }

    public Vector2 subVectors(final Vector2 a, final Vector2 b) {
        this.x = a.width() - b.width();
        this.y = a.height() - b.height();
        return this;
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
