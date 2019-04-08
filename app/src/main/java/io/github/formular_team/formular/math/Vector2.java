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

public class Vector2 {
    protected float x;

    protected float y;

    private static Vector2 _min = new Vector2();

    private static Vector2 _max = new Vector2();

    public Vector2() {
        this(0.0F, 0.0F);
    }

    public Vector2(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * get X coordinate from the vector
     *
     * @return a X coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * get Y coordinate from the vector
     *
     * @return a Y coordinate
     */
    public float getY() {
        return y;
    }

    protected float getZ() {
        return 0.0F;
    }

    /**
     * This method will add specified value to X coordinate of the vector.
     * In another words: x += value.
     *
     * @param x the X coordinate
     */
    void addX(final float x) {
        this.x += x;
    }

    /**
     * This method will add specified value to Y coordinate of the vector.
     * In another words: y += value.
     *
     * @param y the Y coordinate
     */
    void addY(final float y) {
        this.y += y;
    }

    /**
     * This method sets X coordinate of the vector.
     *
     * @param x the X coordinate
     */
    public void setX(final float x) {
        this.x = x;
    }

    /**
     * This method sets Y coordinate of the vector.
     *
     * @param y the Y coordinate
     */
    public void setY(final float y) {
        this.y = y;
    }

    /**
     * Set value of the vector to the specified (X, Y, Z) coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
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
            throw new Error("index is out of range: " + index);
        }
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

    /**
     * Set value of the vector from another vector.
     *
     * @param v the other vector
     * @return the current vector
     */
    public Vector2 copy(final Vector2 v) {
        return this.set(v.x, v.y);
    }

    public Vector2 add(final Vector2 v) {
        return this.add(this, v);
    }

    public Vector2 add(final Vector2 v1, final Vector2 v2) {
        this.x = v1.x + v2.x;
        this.y = v1.y + v2.y;
        return this;
    }

    public Vector2 add(final float s) {
        this.addX(s);
        this.addY(s);
        return this;
    }

    public Vector2 sub(final Vector2 v) {
        return this.sub(this, v);
    }

    public Vector2 sub(final Vector2 v1, final Vector2 v2) {
        this.x = v1.x - v2.x;
        this.y = v1.y - v2.y;
        return this;
    }

    public Vector2 multiply(final Vector2 v) {
        return this.multiply(this, v);
    }

    public Vector2 multiply(final Vector2 v1, final Vector2 v2) {
        this.x = v1.x * v2.x;
        this.y = v1.y * v2.y;
        return this;
    }

    public Vector2 multiply(final float s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    public Vector2 divide(final Vector2 v) {
        return this.divide(this, v);
    }

    public Vector2 divide(final Vector2 v1, final Vector2 v2) {
        this.x = v1.x / v2.x;
        this.y = v1.y / v2.y;
        return this;
    }

    public Vector2 divide(final float s) {
        if (s != 0) {
            this.x /= s;
            this.y /= s;
        } else {
            this.set(0, 0);
        }
        return this;
    }

    public Vector2 min(final Vector2 v) {
        if (this.x > v.x) {
            this.x = v.x;
        }
        if (this.y > v.y) {
            this.y = v.y;
        }
        return this;
    }

    public Vector2 max(final Vector2 v) {
        if (this.x < v.x) {
            this.x = v.x;
        }
        if (this.y < v.y) {
            this.y = v.y;
        }
        return this;
    }

    /**
     * This function assumes getMin &#60; max, if this assumption isn't true it will not operate correctly
     */
    public Vector2 clamp(final Vector2 min, final Vector2 max) {
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
        return this;
    }

    public Vector2 clamp(final float minVal, final float maxVal) {
        _min.set(minVal, minVal);
        _max.set(maxVal, maxVal);
        return this.clamp(_min, _max);
    }

    public Vector2 floor() {
        this.x = Mth.floor(this.x);
        this.y = Mth.floor(this.y);
        return this;

    }

    public Vector2 ceil() {
        this.x = Mth.ceil(this.x);
        this.y = Mth.ceil(this.y);
        return this;

    }

    public Vector2 round() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        return this;

    }

    public Vector2 roundToZero() {
        this.x = (this.x < 0.0F) ? Mth.ceil(this.x) : Mth.floor(this.x);
        this.y = (this.y < 0.0F) ? Mth.ceil(this.y) : Mth.floor(this.y);
        return this;

    }

    /**
     * Negates the value of this vector in place.
     */
    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    /**
     * Computes the dot product of the this vector and vector v1.
     *
     * @param v the other vector
     */
    public float dot(final Vector2 v) {
        return (this.x * v.x + this.y * v.y);
    }

    /**
     * Returns the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    public float lengthSq() {
        return this.dot(this);
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector
     */
    public float length() {
        return Mth.sqrt(this.lengthSq());
    }

    /**
     * Normalizes this vector in place.
     */
    public Vector2 normalize() {
        return this.multiply(1.0F / this.length());
    }

    public float distanceToSquared(final Vector2 v) {
        final float dx = this.x - v.x;
        final float dy = this.y - v.y;
        return (dx * dx + dy * dy);
    }

    public float distanceTo(final Vector2 v) {
        return Mth.sqrt(this.distanceToSquared(v));
    }

    public Vector2 setLength(final float l) {
        final float oldLength = this.length();
        if (oldLength != 0.0F && l != oldLength) {
            this.multiply(l / oldLength);
        }
        return this;
    }

    public Vector2 lerp(final Vector2 v1, final float alpha) {
        this.x += (v1.x - this.x) * alpha;
        this.y += (v1.y - this.y) * alpha;
        return this;
    }

    public float cross(final Vector2 v) {
        return this.x * v.y - this.y * v.x;
    }

    public Vector2 apply(final Matrix3 m) {
        final float  x = this.x, y = this.y;
        final Float32Array e = m.getArray();
        this.x = e.get(0) * x + e.get(3) * y + e.get(6);
        this.y = e.get(1) * x + e.get(4) * y + e.get(7);
        return this;
    }

    public Vector2 rotate() {
        //noinspection SuspiciousNameCombination
        return new Vector2(-this.y, this.x);
    }

    public Vector2 rotateAround(final Vector2 center, final float angle) {
        final float c = Mth.cos(angle), s = Mth.sin(angle);
        final float x = this.x - center.x;
        final float y = this.y - center.y;
        this.x = x * c - y * s + center.x;
        this.y = x * s + y * c + center.y;
        return this;
    }

    public boolean isZero() {
        return this.lengthSq() < 0.0001F;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector2) {
            final Vector2 v1 = (Vector2) obj;
            return (this.x == v1.x && this.y == v1.y);
        }
        return false;
    }

    public Vector2 fromArray(final Float32Array array) {
        return this.fromArray(array, 0);
    }

    public Vector2 fromArray(final Float32Array array, final int offset) {
        this.x = array.get(offset);
        this.y = array.get(offset + 1);
        return this;
    }

    public Float32Array toArray() {
        return this.toArray(Float32Array.create(2), 0);
    }

    public Float32Array toArray(final Float32Array array, final int offset) {

        array.set(offset, this.x);
        array.set(offset + 1, this.y);

        return array;
    }

    public Vector2 clone() {
        return new Vector2(this.x, this.y);
    }

    public boolean equals(final Vector2 v) {
        return ((v.x == this.x) && (v.y == this.y));
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    public Vector2 reflect(final Vector2 normal) {
        return this.sub(normal.clone().multiply(2.0F * this.dot(normal)));
    }
}