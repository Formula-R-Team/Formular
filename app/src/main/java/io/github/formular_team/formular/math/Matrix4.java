package io.github.formular_team.formular.math;

public class Matrix4 {
    private float[] elements;

    public Matrix4() {
        this.elements = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 0
        };
    }

    public Matrix4 copy() {
        final Matrix4 temp = new Matrix4();
        temp.fromArray(this.elements);
        return temp;
    }

    public float[] elements() {
        return this.elements;
    }

    public void compose(final Vector3 position, final Quaternion quaternion, final Vector3 scale) {
        final float[] te = this.elements;

        final float x = quaternion.x();
        final float y = quaternion.y();
        final float z = quaternion.z();
        final float w = quaternion.w();
        final float x2 = x + x;
        final float y2 = y + y;
        final float z2 = z + z;
        final float xx = x * x2;
        final float xy = x * y2;
        final float xz = x * z2;
        final float yy = y * y2;
        final float yz = y * z2;
        final float zz = z * z2;
        final float wx = w * x2;
        final float wy = w * y2;
        final float wz = w * z2;

        final float sx = scale.x();
        final float sy = scale.y();
        final float sz = scale.z();

        te[0] = (1 - (yy + zz)) * sx;
        te[1] = (xy + wz) * sx;
        te[2] = (xz - wy) * sx;
        te[3] = 0;

        te[4] = (xy - wz) * sy;
        te[5] = (1 - (xx + zz)) * sy;
        te[6] = (yz + wx) * sy;
        te[7] = 0;

        te[8] = (xz + wy) * sz;
        te[9] = (yz - wx) * sz;
        te[10] = (1 - (xx + yy)) * sz;
        te[11] = 0;

        te[12] = position.x();
        te[13] = position.y();
        te[14] = position.z();
        te[15] = 1;
    }

    public void copy(final Matrix4 m) {
        final float[] te = this.elements;
        final float[] me = m.elements();

        te[0] = me[0];
        te[1] = me[1];
        te[2] = me[2];
        te[3] = me[3];
        te[4] = me[4];
        te[5] = me[5];
        te[6] = me[6];
        te[7] = me[7];
        te[8] = me[8];
        te[9] = me[9];
        te[10] = me[10];
        te[11] = me[11];
        te[12] = me[12];
        te[13] = me[13];
        te[14] = me[14];
        te[15] = me[15];
    }

    public void copyPosition(final Matrix4 m) {
        final float[] te = this.elements;
        final float[] me = m.elements();

        te[12] = me[12];
        te[13] = me[13];
        te[14] = me[14];

    }

    public void decompose(final Vector3 position, final Quaternion quaternion, final Vector3 scale) {
        final Vector3 vector = new Vector3();
        final Matrix4 matrix = new Matrix4();

        final float[] te = this.elements;

        vector.set(te[0], te[1], te[2]);
        float sx = vector.length();
        vector.set(te[4], te[5], te[6]);
        final float sy = vector.length();
        vector.set(te[8], te[9], te[10]);
        final float sz = vector.length();

        // if determine is negative, we need to invert one scale
        final float det = this.determinant();
        if (det < 0) {
            sx = -sx;
        }

        position.setX(te[12]);
        position.setX(te[13]);
        position.setX(te[14]);

        // scale the rotation part
        matrix.copy(this);

        final float invSX = 1 / sx;
        final float invSY = 1 / sy;
        final float invSZ = 1 / sz;
        final float[] matrixElements = matrix.elements();
        matrixElements[0] *= invSX;
        matrixElements[1] *= invSX;
        matrixElements[2] *= invSX;

        matrixElements[4] *= invSY;
        matrixElements[5] *= invSY;
        matrixElements[6] *= invSY;

        matrixElements[8] *= invSZ;
        matrixElements[9] *= invSZ;
        matrixElements[10] *= invSZ;

        quaternion.setFromRotationMatrix(matrix);

        scale.setX(sx);
        scale.setY(sy);
        scale.setZ(sz);
    }

    public float determinant() {
        final float[] te = this.elements;

        final float n11 = te[0];
        final float n12 = te[4];
        final float n13 = te[8];
        final float n14 = te[12];
        final float n21 = te[1];
        final float n22 = te[5];
        final float n23 = te[9];
        final float n24 = te[13];
        final float n31 = te[2];
        final float n32 = te[6];
        final float n33 = te[10];
        final float n34 = te[14];
        final float n41 = te[3];
        final float n42 = te[7];
        final float n43 = te[11];
        final float n44 = te[15];

        //( based on http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm )

        return (
            n41 * (
                +n14 * n23 * n32
                    - n13 * n24 * n32
                    - n14 * n22 * n33
                    + n12 * n24 * n33
                    + n13 * n22 * n34
                    - n12 * n23 * n34
            ) +
                n42 * (
                    +n11 * n23 * n34
                        - n11 * n24 * n33
                        + n14 * n21 * n33
                        - n13 * n21 * n34
                        + n13 * n24 * n31
                        - n14 * n23 * n31
                ) +
                n43 * (
                    +n11 * n24 * n32
                        - n11 * n22 * n34
                        - n14 * n21 * n32
                        + n12 * n21 * n34
                        + n14 * n22 * n31
                        - n12 * n24 * n31
                ) +
                n44 * (
                    -n13 * n22 * n31
                        - n11 * n23 * n32
                        + n11 * n22 * n33
                        + n13 * n21 * n32
                        - n12 * n21 * n33
                        + n12 * n23 * n31
                )

        );
    }

    public boolean equals(final Matrix4 m) {
        final float[] te = this.elements;
        final float[] me = m.elements();

        for (int i = 0; i < 16; i++) {

            if (te[i] != me[i]) {
                return false;
            }

        }

        return true;
    }

    public void extractBasis(final Vector3 xAxis, final Vector3 yAxis, final Vector3 zAxis) {
        xAxis.setFromMatrixColumn(this, 0);
        yAxis.setFromMatrixColumn(this, 1);
        zAxis.setFromMatrixColumn(this, 2);
    }

    public void extractRotation(final Matrix4 m) {
        final Vector3 v1 = new Vector3();


        // this method does not support reflection matrices

        final float[] te = this.elements;
        final float[] me = m.elements();
        v1.setFromMatrixColumn(m, 0);
        final float scaleX = 1 / v1.length();
        v1.setFromMatrixColumn(m, 0);
        final float scaleY = 1 / v1.length();
        v1.setFromMatrixColumn(m, 0);
        final float scaleZ = 1 / v1.length();

        te[0] = me[0] * scaleX;
        te[1] = me[1] * scaleX;
        te[2] = me[2] * scaleX;
        te[3] = 0;

        te[4] = me[4] * scaleY;
        te[5] = me[5] * scaleY;
        te[6] = me[6] * scaleY;
        te[7] = 0;

        te[8] = me[8] * scaleZ;
        te[9] = me[9] * scaleZ;
        te[10] = me[10] * scaleZ;
        te[11] = 0;

        te[12] = 0;
        te[13] = 0;
        te[14] = 0;
        te[15] = 1;
    }

    public void fromArray(final float[] array) {
        this.fromArray(array, 0);
    }

    public void fromArray(final float[] array, final int offset) {
        System.arraycopy(array, offset, this.elements, 0, 16);
    }

    public Matrix4 getInverse(final Matrix4 m, final boolean throwOnDegenerate) {
        final float[] te = this.elements;
        final float[] me = m.elements();

        final float n11 = me[0];
        final float n21 = me[1];
        final float n31 = me[2];
        final float n41 = me[3];
        final float n12 = me[4];
        final float n22 = me[5];
        final float n32 = me[6];
        final float n42 = me[7];
        final float n13 = me[8];
        final float n23 = me[9];
        final float n33 = me[10];
        final float n43 = me[11];
        final float n14 = me[12];
        final float n24 = me[13];
        final float n34 = me[14];
        final float n44 = me[15];
        final float t11 = n23 * n34 * n42 - n24 * n33 * n42 + n24 * n32 * n43 - n22 * n34 * n43 - n23 * n32 * n44 + n22 * n33 * n44;
        final float t12 = n14 * n33 * n42 - n13 * n34 * n42 - n14 * n32 * n43 + n12 * n34 * n43 + n13 * n32 * n44 - n12 * n33 * n44;
        final float t13 = n13 * n24 * n42 - n14 * n23 * n42 + n14 * n22 * n43 - n12 * n24 * n43 - n13 * n22 * n44 + n12 * n23 * n44;
        final float t14 = n14 * n23 * n32 - n13 * n24 * n32 - n14 * n22 * n33 + n12 * n24 * n33 + n13 * n22 * n34 - n12 * n23 * n34;

        final float det = n11 * t11 + n21 * t12 + n31 * t13 + n41 * t14;

        if (det == 0) {
            if (throwOnDegenerate) {
                throw new RuntimeException("Degenerate");
            }
            return this.identity();
        }

        final float detInv = 1 / det;

        te[0] = t11 * detInv;
        te[1] = (n24 * n33 * n41 - n23 * n34 * n41 - n24 * n31 * n43 + n21 * n34 * n43 + n23 * n31 * n44 - n21 * n33 * n44) * detInv;
        te[2] = (n22 * n34 * n41 - n24 * n32 * n41 + n24 * n31 * n42 - n21 * n34 * n42 - n22 * n31 * n44 + n21 * n32 * n44) * detInv;
        te[3] = (n23 * n32 * n41 - n22 * n33 * n41 - n23 * n31 * n42 + n21 * n33 * n42 + n22 * n31 * n43 - n21 * n32 * n43) * detInv;

        te[4] = t12 * detInv;
        te[5] = (n13 * n34 * n41 - n14 * n33 * n41 + n14 * n31 * n43 - n11 * n34 * n43 - n13 * n31 * n44 + n11 * n33 * n44) * detInv;
        te[6] = (n14 * n32 * n41 - n12 * n34 * n41 - n14 * n31 * n42 + n11 * n34 * n42 + n12 * n31 * n44 - n11 * n32 * n44) * detInv;
        te[7] = (n12 * n33 * n41 - n13 * n32 * n41 + n13 * n31 * n42 - n11 * n33 * n42 - n12 * n31 * n43 + n11 * n32 * n43) * detInv;

        te[8] = t13 * detInv;
        te[9] = (n14 * n23 * n41 - n13 * n24 * n41 - n14 * n21 * n43 + n11 * n24 * n43 + n13 * n21 * n44 - n11 * n23 * n44) * detInv;
        te[10] = (n12 * n24 * n41 - n14 * n22 * n41 + n14 * n21 * n42 - n11 * n24 * n42 - n12 * n21 * n44 + n11 * n22 * n44) * detInv;
        te[11] = (n13 * n22 * n41 - n12 * n23 * n41 - n13 * n21 * n42 + n11 * n23 * n42 + n12 * n21 * n43 - n11 * n22 * n43) * detInv;

        te[12] = t14 * detInv;
        te[13] = (n13 * n24 * n31 - n14 * n23 * n31 + n14 * n21 * n33 - n11 * n24 * n33 - n13 * n21 * n34 + n11 * n23 * n34) * detInv;
        te[14] = (n14 * n22 * n31 - n12 * n24 * n31 - n14 * n21 * n32 + n11 * n24 * n32 + n12 * n21 * n34 - n11 * n22 * n34) * detInv;
        te[15] = (n12 * n23 * n31 - n13 * n22 * n31 + n13 * n21 * n32 - n11 * n23 * n32 - n12 * n21 * n33 + n11 * n22 * n33) * detInv;
        return m;
    }

    public float getMaxScaleOnAxis() {
        final float[] te = this.elements;
        final float scaleXSq = te[0] * te[0] + te[1] * te[1] + te[2] * te[2];
        final float scaleYSq = te[4] * te[4] + te[5] * te[5] + te[6] * te[6];
        final float scaleZSq = te[8] * te[8] + te[9] * te[9] + te[10] * te[10];
        return Mth.sqrt(Math.max(Math.max(scaleXSq, scaleYSq), scaleZSq));
    }

    public Matrix4 identity() {
        this.elements = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 0
        };
        return this;
    }

    public void lookAt(final Vector3 eye, final Vector3 target, final Vector3 up) {
        final Vector3 x = new Vector3();
        final Vector3 y = new Vector3();
        final Vector3 z = new Vector3();


        final float[] te = this.elements;

        z.subVectors(eye, target);

        if (z.lengthSq() == 0) {

            // eye and target are in the same position

            z.setZ(1f);

        }

        z.normalize();
        x.crossVectors(up, z);

        if (x.lengthSq() == 0) {

            // up and z are parallel

            if (Math.abs(up.z()) == 1) {

                z.setX(z.x() + 0.0001f);

            } else {

                z.setZ(z.z() + 0.0001f);

            }

            z.normalize();
            x.crossVectors(up, z);

        }

        x.normalize();
        y.crossVectors(z, x);

        te[0] = x.x();
        te[4] = y.x();
        te[8] = z.x();
        te[1] = x.y();
        te[5] = y.y();
        te[9] = z.y();
        te[2] = x.z();
        te[6] = y.z();
        te[10] = z.z();
    }

    public void makeRotationAxis(final Vector3 axis, final float angle) {
        // Based on http://www.gamedev.net/reference/articles/article1199.asp

        final float c = Mth.cos(angle);
        final float s = Mth.sin(angle);
        final float t = 1f - c;
        final float x = axis.x();
        final float y = axis.y();
        final float z = axis.z();
        final float tx = t * x;
        final float ty = t * y;

        this.set(

            tx * x + c, tx * y - s * z, tx * z + s * y, 0,
            tx * y + s * z, ty * y + c, ty * z - s * x, 0,
            tx * z - s * y, ty * z + s * x, t * z * z + c, 0,
            0, 0, 0, 1

        );
    }

    public void makeBasis(final Vector3 xAxis, final Vector3 yAxis, final Vector3 zAxis) {
        this.set(
            xAxis.x(), yAxis.x(), zAxis.x(), 0,
            xAxis.y(), yAxis.y(), zAxis.y(), 0,
            xAxis.z(), yAxis.z(), zAxis.z(), 0,
            0, 0, 0, 1
        );
    }

    public void makePerspective(final float left, final float right, final float top, final float bottom, final float near, final float far) {
        final float[] te = this.elements;
        final float x = 2 * near / (right - left);
        final float y = 2 * near / (top - bottom);

        final float a = (right + left) / (right - left);
        final float b = (top + bottom) / (top - bottom);
        final float c = -(far + near) / (far - near);
        final float d = -2 * far * near / (far - near);

        te[0] = x;
        te[4] = 0;
        te[8] = a;
        te[12] = 0;
        te[1] = 0;
        te[5] = y;
        te[9] = b;
        te[13] = 0;
        te[2] = 0;
        te[6] = 0;
        te[10] = c;
        te[14] = d;
        te[3] = 0;
        te[7] = 0;
        te[11] = -1;
        te[15] = 0;
    }

    public void makeOrthographic(final float left, final float right, final float top, final float bottom, final float near, final float far) {
        final float[] te = this.elements;
        final float w = 1.0f / (right - left);
        final float h = 1.0f / (top - bottom);
        final float p = 1.0f / (far - near);

        final float x = (right + left) * w;
        final float y = (top + bottom) * h;
        final float z = (far + near) * p;

        te[0] = 2 * w;
        te[4] = 0;
        te[8] = 0;
        te[12] = -x;
        te[1] = 0;
        te[5] = 2 * h;
        te[9] = 0;
        te[13] = -y;
        te[2] = 0;
        te[6] = 0;
        te[10] = -2 * p;
        te[14] = -z;
        te[3] = 0;
        te[7] = 0;
        te[11] = 0;
        te[15] = 1;
    }

    public void makeRotationFromQuaternion(final Quaternion q) {
        final Vector3 zero = new Vector3(0, 0, 0);
        final Vector3 one = new Vector3(1, 1, 1);

        this.compose(zero, q, one);
    }

    public void makeRotationX(final float theta) {
        final float c = Mth.cos(theta);
        final float s = Mth.sin(theta);

        this.set(

            1, 0, 0, 0,
            0, c, -s, 0,
            0, s, c, 0,
            0, 0, 0, 1

        );
    }

    public void makeRotationY(final float theta) {
        final float c = Mth.cos(theta);
        final float s = Mth.sin(theta);

        this.set(

            c, 0, s, 0,
            0, 1, 0, 0,
            -s, 0, c, 0,
            0, 0, 0, 1

        );
    }

    public void makeRotationZ(final float theta) {
        final float c = Mth.cos(theta);
        final float s = Mth.sin(theta);

        this.set(

            c, -s, 0, 0,
            s, c, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1

        );
    }

    public void makeScale(final float x, final float y, final float z) {
        this.set(

            x, 0, 0, 0,
            0, y, 0, 0,
            0, 0, z, 0,
            0, 0, 0, 1

        );
    }

    public void makeShear(final float x, final float y, final float z) {
        this.set(

            1, y, z, 0,
            x, 1, z, 0,
            x, y, 1, 0,
            0, 0, 0, 1

        );
    }

    public void makeTranslation(final float x, final float y, final float z) {
        this.set(

            1, 0, 0, x,
            0, 1, 0, y,
            0, 0, 1, z,
            0, 0, 0, 1

        );
    }

    public Matrix4 multiply(final Matrix4 m) {
        return this.multiplyMatrices(this, m);
    }

    public Matrix4 multiplyMatrices(final Matrix4 a, final Matrix4 b) {
        final float[] ae = a.elements();
        final float[] be = b.elements();
        final float[] te = this.elements;

        final float a11 = ae[0];
        final float a12 = ae[4];
        final float a13 = ae[8];
        final float a14 = ae[12];
        final float a21 = ae[1];
        final float a22 = ae[5];
        final float a23 = ae[9];
        final float a24 = ae[13];
        final float a31 = ae[2];
        final float a32 = ae[6];
        final float a33 = ae[10];
        final float a34 = ae[14];
        final float a41 = ae[3];
        final float a42 = ae[7];
        final float a43 = ae[11];
        final float a44 = ae[15];

        final float b11 = be[0];
        final float b12 = be[4];
        final float b13 = be[8];
        final float b14 = be[12];
        final float b21 = be[1];
        final float b22 = be[5];
        final float b23 = be[9];
        final float b24 = be[13];
        final float b31 = be[2];
        final float b32 = be[6];
        final float b33 = be[10];
        final float b34 = be[14];
        final float b41 = be[3];
        final float b42 = be[7];
        final float b43 = be[11];
        final float b44 = be[15];

        te[0] = a11 * b11 + a12 * b21 + a13 * b31 + a14 * b41;
        te[4] = a11 * b12 + a12 * b22 + a13 * b32 + a14 * b42;
        te[8] = a11 * b13 + a12 * b23 + a13 * b33 + a14 * b43;
        te[12] = a11 * b14 + a12 * b24 + a13 * b34 + a14 * b44;

        te[1] = a21 * b11 + a22 * b21 + a23 * b31 + a24 * b41;
        te[5] = a21 * b12 + a22 * b22 + a23 * b32 + a24 * b42;
        te[9] = a21 * b13 + a22 * b23 + a23 * b33 + a24 * b43;
        te[13] = a21 * b14 + a22 * b24 + a23 * b34 + a24 * b44;

        te[2] = a31 * b11 + a32 * b21 + a33 * b31 + a34 * b41;
        te[6] = a31 * b12 + a32 * b22 + a33 * b32 + a34 * b42;
        te[10] = a31 * b13 + a32 * b23 + a33 * b33 + a34 * b43;
        te[14] = a31 * b14 + a32 * b24 + a33 * b34 + a34 * b44;

        te[3] = a41 * b11 + a42 * b21 + a43 * b31 + a44 * b41;
        te[7] = a41 * b12 + a42 * b22 + a43 * b32 + a44 * b42;
        te[11] = a41 * b13 + a42 * b23 + a43 * b33 + a44 * b43;
        te[15] = a41 * b14 + a42 * b24 + a43 * b34 + a44 * b44;
        return this;
    }

    public void multiplyScalar(final float s) {
        final float[] te = this.elements;

        te[0] *= s;
        te[4] *= s;
        te[8] *= s;
        te[12] *= s;
        te[1] *= s;
        te[5] *= s;
        te[9] *= s;
        te[13] *= s;
        te[2] *= s;
        te[6] *= s;
        te[10] *= s;
        te[14] *= s;
        te[3] *= s;
        te[7] *= s;
        te[11] *= s;
        te[15] *= s;
    }

    public void premultiply(final Matrix4 m) {
        this.multiplyMatrices(m, this);

    }

    public void scale(final Vector3 v) {
        final float[] te = this.elements;
        final float x = v.x();
        final float y = v.y();
        final float z = v.z();

        te[0] *= x;
        te[4] *= y;
        te[8] *= z;
        te[1] *= x;
        te[5] *= y;
        te[9] *= z;
        te[2] *= x;
        te[6] *= y;
        te[10] *= z;
        te[3] *= x;
        te[7] *= y;
        te[11] *= z;
    }

    public void set(final float n11, final float n12, final float n13, final float n14, final float n21, final float n22, final float n23, final float n24, final float n31, final float n32, final float n33, final float n34, final float n41, final float n42, final float n43, final float n44) {
        this.elements = new float[] {
            n11, n12, n13, n14,
            n21, n22, n23, n24,
            n31, n32, n33, n34,
            n41, n42, n43, n44
        };
    }

    public Matrix4 setPostion(final Vector3 v) {
        final float[] te = this.elements;
        te[12] = v.x();
        te[13] = v.y();
        te[14] = v.z();
        return this;
    }

    public float[] toArray() {
        return this.toArray(new float[16], 0);
    }

    public float[] toArray(final float[] array) {
        return this.toArray(array, 0);
    }

    public float[] toArray(final float[] array, final int offset) {
        final float[] te = this.elements;

        array[offset] = te[0];
        array[offset + 1] = te[1];
        array[offset + 2] = te[2];
        array[offset + 3] = te[3];

        array[offset + 4] = te[4];
        array[offset + 5] = te[5];
        array[offset + 6] = te[6];
        array[offset + 7] = te[7];

        array[offset + 8] = te[8];
        array[offset + 9] = te[9];
        array[offset + 10] = te[10];
        array[offset + 11] = te[11];

        array[offset + 12] = te[12];
        array[offset + 13] = te[13];
        array[offset + 14] = te[14];
        array[offset + 15] = te[15];

        return array;
    }

    public void transpose() {
        final float[] te = this.elements;
        float tmp;

        tmp = te[1];
        te[1] = te[4];
        te[4] = tmp;
        tmp = te[2];
        te[2] = te[8];
        te[8] = tmp;
        tmp = te[6];
        te[6] = te[9];
        te[9] = tmp;

        tmp = te[3];
        te[3] = te[12];
        te[12] = tmp;
        tmp = te[7];
        te[7] = te[13];
        te[13] = tmp;
        tmp = te[11];
        te[11] = te[14];
        te[14] = tmp;
    }
}
