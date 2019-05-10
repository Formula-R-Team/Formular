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
    private final float[] elements;

    private static final Vector3 _v1 = new Vector3();

    public Matrix3() {
        this.elements = new float[9];
        this.identity();
    }

    public Matrix3(final float n11, final float n12, final float n13, final float n21, final float n22, final float n23, final float n31, final float n32, final float n33) {
        this();
        this.set(n11, n12, n13, n21, n22, n23, n31, n32, n33);
    }

    public Matrix3 set(final float n11, final float n12, final float n13, final float n21, final float n22, final float n23, final float n31, final float n32, final float n33) {
        final float[] te = this.getArray();
        te[0] = n11;
        te[3] = n12;
        te[6] = n13;
        te[1] = n21;
        te[4] = n22;
        te[7] = n23;
        te[2] = n31;
        te[5] = n32;
        te[8] = n33;
        return this;
    }

    public float[] getArray() {
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
        final float[] te = this.getArray();
        te[0] = te[0] * sx; te[3] = te[3] * sx; te[6] = te[6] * sx;
        te[1] = te[1] * sx; te[4] = te[4] * sy; te[7] = te[7] * sy;
        return this;
    }

    public Matrix3 rotate(final float theta) {
        final float c = Mth.cos(theta);
        final float s = Mth.sin(theta);
        final float[] te = this.getArray();
        final float a11 = te[0], a12 = te[3], a13 = te[6];
        final float a21 = te[1], a22 = te[4], a23 = te[7];
        te[0] = c * a11 + s * a21;
        te[3] = c * a12 + s * a22;
        te[6] = c * a13 + s * a23;
        te[1] = -s * a11 + c * a21;
        te[4] = -s * a12 + c * a22;
        te[7] = -s * a13 + c * a23;
        return this;
    }

    public Matrix3 translate(final float tx, final float ty) {
        final float[] te = this.getArray();
        te[0] = te[0] + tx * te[2]; te[3] = te[3] + tx * te[5]; te[6] = te[6] + tx * te[8];
        te[1] = te[1] + ty * te[2]; te[4] = te[4] + ty * te[5]; te[7] = te[7] + ty * te[8];
        return this;
    }

    public Matrix3 copy(final Matrix3 m) {
        final float[] me = m.getArray();
        this.set(
            me[0], me[3], me[6],
            me[1], me[4], me[7],
            me[2], me[5], me[8]
        );
        return this;
    }

    public float[] applyToVector3Array(final float[] array) {
        return this.applyToVector3Array(array, 0, array.length);
    }

    public float[] applyToVector3Array(final float[] array, final int offset, final int length) {
        for (int i = 0, j = offset; i < length; i += 3, j += 3) {
            _v1.x = array[j];
            _v1.y = array[j + 1];
            _v1.z = array[j + 2];
            _v1.apply(this);
            array[j] = _v1.x;
            array[j + 1] = _v1.y;
            array[j + 2] = _v1.z;
        }
        return array;
    }

    public Matrix3 multiply(final Matrix3 m) {
        return this.multiply(m, this);
    }

    public Matrix3 multiply(final Matrix3 a, final Matrix3 b) {
        final float[] ae = a.getArray();
        final float[] be = b.getArray();
        final float[] te = this.getArray();
        final float a11 = ae[0], a12 = ae[3], a13 = ae[6];
        final float a21 = ae[1], a22 = ae[4], a23 = ae[7];
        final float a31 = ae[2], a32 = ae[5], a33 = ae[8];
        final float b11 = be[0], b12 = be[3], b13 = be[6];
        final float b21 = be[1], b22 = be[4], b23 = be[7];
        final float b31 = be[2], b32 = be[5], b33 = be[8];
        te[0] = a11 * b11 + a12 * b21 + a13 * b31;
        te[3] = a11 * b12 + a12 * b22 + a13 * b32;
        te[6] = a11 * b13 + a12 * b23 + a13 * b33;
        te[1] = a21 * b11 + a22 * b21 + a23 * b31;
        te[4] = a21 * b12 + a22 * b22 + a23 * b32;
        te[7] = a21 * b13 + a22 * b23 + a23 * b33;
        te[2] = a31 * b11 + a32 * b21 + a33 * b31;
        te[5] = a31 * b12 + a32 * b22 + a33 * b32;
        te[8] = a31 * b13 + a32 * b23 + a33 * b33;
        return this;
    }

    public float determinant() {
        final float[] te = this.getArray();
        final float a = te[0];
        final float b = te[1];
        final float c = te[2];
        final float d = te[3];
        final float e = te[4];
        final float f = te[5];
        final float g = te[6];
        final float h = te[7];
        final float i = te[8];
        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g;
    }

    public Matrix3 getInverse(final Matrix4 m) {
        final float[] me = m.getArray();
        final float[] te = this.getArray();
        te[0] = me[10] * me[5] - me[6] * me[9];
        te[1] = -me[10] * me[1] + me[2] * me[9];
        te[2] = me[6] * me[1] - me[2] * me[5];
        te[3] = -me[10] * me[4] + me[6] * me[8];
        te[4] = me[10] * me[0] - me[2] * me[8];
        te[5] = -me[6] * me[0] + me[2] * me[4];
        te[6] = me[9] * me[4] - me[5] * me[8];
        te[7] = -me[9] * me[0] + me[1] * me[8];
        te[8] = me[5] * me[0] - me[1] * me[4];
        final float det = me[0] * te[0] + me[1] * te[3] + me[2] * te[6];
        if (det == 0.0F) {
            this.identity();
        } else {
            this.scale(1.0F / det);
        }
        return this;
    }

    public Matrix3 transpose() {
        float tmp;
        final float[] m = this.getArray();
        tmp = m[1];
        m[1] = m[3];
        m[3] = tmp;
        tmp = m[2];
        m[2] = m[6];
        m[6] = tmp;
        tmp = m[5];
        m[5] = m[7];
        m[7] = tmp;
        return this;
    }

    public Matrix3 getNormalMatrix(final Matrix4 m) {
        return this.getInverse(m).transpose();
    }

    public float[] transposeIntoArray() {
        final float[] r = new float[9];
        final float[] m = this.getArray();
        r[0] = m[0];
        r[1] = m[3];
        r[2] = m[6];
        r[3] = m[1];
        r[4] = m[4];
        r[5] = m[7];
        r[6] = m[2];
        r[7] = m[5];
        r[8] = m[8];
        return r;
    }

    @Override
    public String toString() {
        final StringBuilder retval = new StringBuilder("[");
        for (int i = 0; i < this.getArray().length; i++) {
            retval.append(this.getArray()[i]).append(", ");
        }
        return retval + "]";
    }

    public Matrix3 copy() {
        final float[] te = this.getArray();
        return new Matrix3(
            te[0], te[3], te[6],
            te[1], te[4], te[7],
            te[2], te[5], te[8]
        );
    }
}