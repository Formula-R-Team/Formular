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

public class Matrix3 {
    private final Float32Array elements;

    private static final Vector3 _v1 = new Vector3();

    public Matrix3() {
        this.elements = Float32Array.create(9);
        this.identity();
    }

    public Matrix3(final float n11, final float n12, final float n13, final float n21, final float n22, final float n23, final float n31, final float n32, final float n33) {
        this();
        this.set(n11, n12, n13, n21, n22, n23, n31, n32, n33);
    }

    public Matrix3 set(final float n11, final float n12, final float n13, final float n21, final float n22, final float n23, final float n31, final float n32, final float n33) {
        final Float32Array te = this.getArray();
        te.set(0, n11);
        te.set(3, n12);
        te.set(6, n13);
        te.set(1, n21);
        te.set(4, n22);
        te.set(7, n23);
        te.set(2, n31);
        te.set(5, n32);
        te.set(8, n33);
        return this;
    }

    public Float32Array getArray() {
        return this.elements;
    }

    public Matrix3 identity() {
        this.set(
            1.0F, 0.0F, 0.0F,
            0.0F, 1.0F, 0.0F,
            0.0F, 0.0F, 1.0F
        );
        return this;
    }

    public Matrix3 scale(final float s) {
        return this.scale(s, s);
    }

    public Matrix3 scale(final float sx, final float sy) {
        final Float32Array te = this.getArray();
        te.set(0, te.get(0) * sx); te.set(3, te.get(3) * sx); te.set(6, te.get(6) * sx);
        te.set(1, te.get(1) * sx); te.set(4, te.get(4) * sy); te.set(7, te.get(7) * sy);
        return this;
    }

    public Matrix3 rotate(final float theta) {
        final float c = Mth.cos(theta);
        final float s = Mth.sin(theta);
        final Float32Array te = this.getArray();
        final float a11 = te.get(0), a12 = te.get(3), a13 = te.get(6);
        final float a21 = te.get(1), a22 = te.get(4), a23 = te.get(7);
        te.set(0, c * a11 + s * a21);
        te.set(3, c * a12 + s * a22);
        te.set(6, c * a13 + s * a23);
        te.set(1, -s * a11 + c * a21);
        te.set(4, -s * a12 + c * a22);
        te.set(7, -s * a13 + c * a23);
        return this;
    }

    public Matrix3 translate(final float tx, final float ty) {
        final Float32Array te = this.getArray();
        te.set(0, te.get(0) + tx * te.get(2)); te.set(3, te.get(3) + tx * te.get(5)); te.set(6, te.get(6) + tx * te.get(8));
        te.set(1, te.get(1) + ty * te.get(2)); te.set(4, te.get(4) + ty * te.get(5)); te.set(7, te.get(7) + ty * te.get(8));
        return this;
    }

    public Matrix3 copy(final Matrix3 m) {
        final Float32Array me = m.getArray();
        this.set(
            me.get(0), me.get(3), me.get(6),
            me.get(1), me.get(4), me.get(7),
            me.get(2), me.get(5), me.get(8)
        );
        return this;
    }

    public Float32Array applyToVector3Array(final Float32Array array) {
        return this.applyToVector3Array(array, 0, array.getLength());
    }

    public Float32Array applyToVector3Array(final Float32Array array, final int offset, final int length) {
        for (int i = 0, j = offset; i < length; i += 3, j += 3) {
            _v1.x = array.get(j);
            _v1.y = array.get(j + 1);
            _v1.z = array.get(j + 2);
            _v1.apply(this);
            array.set(j, _v1.x);
            array.set(j + 1, _v1.y);
            array.set(j + 2, _v1.z);
        }
        return array;
    }

    public Matrix3 multiply(final Matrix3 m) {
        return this.multiply(m, this);
    }

    public Matrix3 multiply(final Matrix3 a, final Matrix3 b) {
        final Float32Array ae = a.getArray();
        final Float32Array be = b.getArray();
        final Float32Array te = this.getArray();
        final float a11 = ae.get(0), a12 = ae.get(3), a13 = ae.get(6);
        final float a21 = ae.get(1), a22 = ae.get(4), a23 = ae.get(7);
        final float a31 = ae.get(2), a32 = ae.get(5), a33 = ae.get(8);
        final float b11 = be.get(0), b12 = be.get(3), b13 = be.get(6);
        final float b21 = be.get(1), b22 = be.get(4), b23 = be.get(7);
        final float b31 = be.get(2), b32 = be.get(5), b33 = be.get(8);
        te.set(0, a11 * b11 + a12 * b21 + a13 * b31);
        te.set(3, a11 * b12 + a12 * b22 + a13 * b32);
        te.set(6, a11 * b13 + a12 * b23 + a13 * b33);
        te.set(1, a21 * b11 + a22 * b21 + a23 * b31);
        te.set(4, a21 * b12 + a22 * b22 + a23 * b32);
        te.set(7, a21 * b13 + a22 * b23 + a23 * b33);
        te.set(2, a31 * b11 + a32 * b21 + a33 * b31);
        te.set(5, a31 * b12 + a32 * b22 + a33 * b32);
        te.set(8, a31 * b13 + a32 * b23 + a33 * b33);
        return this;
    }
    public float determinant() {
        final Float32Array te = this.getArray();
        final float a = te.get(0);
        final float b = te.get(1);
        final float c = te.get(2);
        final float d = te.get(3);
        final float e = te.get(4);
        final float f = te.get(5);
        final float g = te.get(6);
        final float h = te.get(7);
        final float i = te.get(8);
        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g;
    }

    public Matrix3 getInverse(final Matrix4 m) {
        final Float32Array me = m.getArray();
        final Float32Array te = this.getArray();
        te.set(0, me.get(10) * me.get(5) - me.get(6) * me.get(9));
        te.set(1, -me.get(10) * me.get(1) + me.get(2) * me.get(9));
        te.set(2, me.get(6) * me.get(1) - me.get(2) * me.get(5));
        te.set(3, -me.get(10) * me.get(4) + me.get(6) * me.get(8));
        te.set(4, me.get(10) * me.get(0) - me.get(2) * me.get(8));
        te.set(5, -me.get(6) * me.get(0) + me.get(2) * me.get(4));
        te.set(6, me.get(9) * me.get(4) - me.get(5) * me.get(8));
        te.set(7, -me.get(9) * me.get(0) + me.get(1) * me.get(8));
        te.set(8, me.get(5) * me.get(0) - me.get(1) * me.get(4));
        final float det = me.get(0) * te.get(0) + me.get(1) * te.get(3) + me.get(2) * te.get(6);
        if (det == 0.0F) {
//            Log.error("Matrix3.invert(): determinant == 0");
            this.identity();
        } else {
            this.scale(1.0F / det);
        }
        return this;
    }

    public Matrix3 transpose() {
        float tmp;
        final Float32Array m = this.getArray();
        tmp = m.get(1);
        m.set(1, m.get(3));
        m.set(3, tmp);
        tmp = m.get(2);
        m.set(2, m.get(6));
        m.set(6, tmp);
        tmp = m.get(5);
        m.set(5, m.get(7));
        m.set(7, tmp);
        return this;
    }

    public Matrix3 getNormalMatrix(final Matrix4 m) {
        return this.getInverse(m).transpose();
    }

    public Float32Array transposeIntoArray() {
        final Float32Array r = Float32Array.create(9);
        final Float32Array m = this.getArray();
        r.set(0, m.get(0));
        r.set(1, m.get(3));
        r.set(2, m.get(6));
        r.set(3, m.get(1));
        r.set(4, m.get(4));
        r.set(5, m.get(7));
        r.set(6, m.get(2));
        r.set(7, m.get(5));
        r.set(8, m.get(8));
        return r;
    }

    @Override
    public String toString() {
        final StringBuilder retval = new StringBuilder("[");
        for (int i = 0; i < this.getArray().getLength(); i++) {
            retval.append(this.getArray().get(i)).append(", ");
        }
        return retval + "]";
    }

    @Override
    public Matrix3 clone() {
        final Float32Array te = this.getArray();
        return new Matrix3(
            te.get(0), te.get(3), te.get(6),
            te.get(1), te.get(4), te.get(7),
            te.get(2), te.get(5), te.get(8)
        );
    }
}