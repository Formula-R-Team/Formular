package io.github.formular_team.formular.math;

public class Matrix3
{
    private float[] elements;

    public Matrix3()
    {
        this.elements = new float[]{1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f};
    }

    public float[] elements()
    {
        return this.elements;
    }

    public boolean isMatrix3()
    {
        return true;
    }

    public void set(final float n11, final float n12, final float n13, final float n21, final float n22, final float n23, final float n31, final float n32, final float n33)
    {
        this.elements[0] = n11;
        this.elements[1] = n12;
        this.elements[2] = n13;
        this.elements[3] = n21;
        this.elements[4] = n22;
        this.elements[5] = n23;
        this.elements[6] = n31;
        this.elements[7] = n32;
        this.elements[8] = n33;
    }

    public Matrix3 clone()
    {
        final Matrix3 temp = new Matrix3();
        temp.fromArray(this.elements);
        return temp;
    }

    public void copy(final Matrix3 m)
    {
        final float[] te = this.elements;
        final float[] me = m.elements();

        te[ 0 ] = me[ 0 ]; te[ 1 ] = me[ 1 ]; te[ 2 ] = me[ 2 ];
        te[ 3 ] = me[ 3 ]; te[ 4 ] = me[ 4 ]; te[ 5 ] = me[ 5 ];
        te[ 6 ] = me[ 6 ]; te[ 7 ] = me[ 7 ]; te[ 8 ] = me[ 8 ];
    }

    public float determinant()
    {
        final float[] te= this.elements;

        final float a = te[ 0 ];
        final float b = te[ 1 ];
        final float c = te[ 2 ];
        final float d = te[ 3 ];
        final float e = te[ 4 ];
        final float f = te[ 5 ];
        final float g = te[ 6 ];
        final float h = te[ 7 ];
        final float i = te[ 8 ];

        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g;
    }

    public boolean equals(final Matrix3 m)
    {
        return this.elements[0] == m.elements()[0] && this.elements[1] == m.elements()[1] && this.elements[2] == m.elements()[2] &&
            this.elements[3] == m.elements()[3] && this.elements[4] == m.elements()[4] && this.elements[5] == m.elements()[5] &&
            this.elements[6] == m.elements()[6] && this.elements[7] == m.elements()[7] && this.elements[8] == m.elements()[8];
    }

    public void fromArray(final float[] array)
    {
        System.arraycopy(array, 0, this.elements, 0, 9);
    }

    public void fromArray(final float[] array, final int offset)
    {
        System.arraycopy(array, offset, this.elements, 0, 9);
    }

    public Matrix3 getInverse(final Matrix3 m)
    {
        final float[] me = m.elements();
        final float[] te = this.elements;

        final float  n11 = me[ 0 ];
        final float n21 = me[ 1 ];
        final float n31 = me[ 2 ];
        final float n12 = me[ 3 ];
        final float n22 = me[ 4 ];
        final float n32 = me[ 5 ];
        final float n13 = me[ 6 ];
        final float n23 = me[ 7 ];
        final float n33 = me[ 8 ];
        final float t11 = n33 * n22 - n32 * n23;
        final float t12 = n32 * n13 - n33 * n12;
        final float t13 = n23 * n12 - n22 * n13;
        final float det = n11 * t11 + n21 * t12 + n31 * t13;

        if (det == 0) {
            return this.identity();
        }

        final float detInv = 1 / det;

        te[ 0 ] = t11 * detInv;
        te[ 1 ] = ( n31 * n23 - n33 * n21 ) * detInv;
        te[ 2 ] = ( n32 * n21 - n31 * n22 ) * detInv;

        te[ 3 ] = t12 * detInv;
        te[ 4 ] = ( n33 * n11 - n31 * n13 ) * detInv;
        te[ 5 ] = ( n31 * n12 - n32 * n11 ) * detInv;

        te[ 6 ] = t13 * detInv;
        te[ 7 ] = ( n21 * n13 - n23 * n11 ) * detInv;
        te[ 8 ] = ( n22 * n11 - n21 * n12 ) * detInv;
        return this;
    }

    public void getInverse(final Matrix3 m, final boolean throwOnDegenerate)
    {
        final float[] me = m.elements();
        final float[] te = this.elements;

        final float  n11 = me[ 0 ];
        final float n21 = me[ 1 ];
        final float n31 = me[ 2 ];
        final float n12 = me[ 3 ];
        final float n22 = me[ 4 ];
        final float n32 = me[ 5 ];
        final float n13 = me[ 6 ];
        final float n23 = me[ 7 ];
        final float n33 = me[ 8 ];
        final float t11 = n33 * n22 - n32 * n23;
        final float t12 = n32 * n13 - n33 * n12;
        final float t13 = n23 * n12 - n22 * n13;
        final float det = n11 * t11 + n21 * t12 + n31 * t13;

        if ( det == 0 ) {

            final String msg = "THREE.Matrix3: .getInverse() can't invert matrix, determinant is 0";

            if (throwOnDegenerate) {

                throw new Error( msg );

            }

            this.identity();

        }

        final float detInv = 1 / det;

        te[ 0 ] = t11 * detInv;
        te[ 1 ] = ( n31 * n23 - n33 * n21 ) * detInv;
        te[ 2 ] = ( n32 * n21 - n31 * n22 ) * detInv;

        te[ 3 ] = t12 * detInv;
        te[ 4 ] = ( n33 * n11 - n31 * n13 ) * detInv;
        te[ 5 ] = ( n31 * n12 - n32 * n11 ) * detInv;

        te[ 6 ] = t13 * detInv;
        te[ 7 ] = ( n21 * n13 - n23 * n11 ) * detInv;
        te[ 8 ] = ( n22 * n11 - n21 * n12 ) * detInv;
    }

    public Matrix3 getNormalMatrix(final Matrix4 m) {
        return this.setFromMatrix4(m).getInverse(this).transpose();
    }

    public Matrix3 setFromMatrix4(final Matrix4 m ) {
        final float[] me = m.elements();
        this.set(
                me[0], me[4], me[8],
                me[1], me[5], me[9],
                me[2], me[6], me[10]
        );
        return this;
    }

    public Matrix3 identity() {
        this.elements = new float[]{ 1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f };
        return this;
    }

    public void multiply(final Matrix3 m)
    {
        this.multiplyMatrices( this, m );
    }

    public void multiplyMatrices(final Matrix3 a, final Matrix3 b)
    {
        final float[] ae = a.elements();
        final float[] be = b.elements();
        final float[] te = this.elements();

        final float a11 = ae[ 0 ];
        final float a12 = ae[ 3 ];
        final float a13 = ae[ 6 ];
        final float a21 = ae[ 1 ];
        final float a22 = ae[ 4 ];
        final float a23 = ae[ 7 ];
        final float a31 = ae[ 2 ];
        final float a32 = ae[ 5 ];
        final float a33 = ae[ 8 ];

        final float b11 = be[ 0 ];
        final float b12 = be[ 3 ];
        final float b13 = be[ 6 ];
        final float b21 = be[ 1 ];
        final float b22 = be[ 4 ];
        final float b23 = be[ 7 ];
        final float b31 = be[ 2 ];
        final float b32 = be[ 5 ];
        final float b33 = be[ 8 ];

        te[ 0 ] = a11 * b11 + a12 * b21 + a13 * b31;
        te[ 3 ] = a11 * b12 + a12 * b22 + a13 * b32;
        te[ 6 ] = a11 * b13 + a12 * b23 + a13 * b33;

        te[ 1 ] = a21 * b11 + a22 * b21 + a23 * b31;
        te[ 4 ] = a21 * b12 + a22 * b22 + a23 * b32;
        te[ 7 ] = a21 * b13 + a22 * b23 + a23 * b33;

        te[ 2 ] = a31 * b11 + a32 * b21 + a33 * b31;
        te[ 5 ] = a31 * b12 + a32 * b22 + a33 * b32;
        te[ 8 ] = a31 * b13 + a32 * b23 + a33 * b33;
    }

    public void multiplyScalar(final float s)
    {
        final float[] te = this.elements;

        te[ 0 ] *= s; te[ 3 ] *= s; te[ 6 ] *= s;
        te[ 1 ] *= s; te[ 4 ] *= s; te[ 7 ] *= s;
        te[ 2 ] *= s; te[ 5 ] *= s; te[ 8 ] *= s;
    }

    public void premultiply(final Matrix3 m)
    {
        this.multiplyMatrices( m, this );
    }

    public void setUvTransform(final float tx, final float ty, final float sx, final float sy, final float rotation, final float cx, final float cy)
    {
        final float c =  Mth.cos( rotation );
        final float s =  Mth.sin( rotation );

        this.set(
                sx * c, sx * s, - sx * ( c * cx + s * cy ) + cx + tx,
                - sy * s, sy * c, - sy * ( - s * cx + c * cy ) + cy + ty,
                0, 0, 1
        );
    }

    public float[] toArray()
    {
       final float[] array = new float[9];
       final int offset=0;

       final float[] te = this.elements;

       array[ offset ] = te[ 0 ];
       array[ offset + 1 ] = te[ 1 ];
       array[ offset + 2 ] = te[ 2 ];

       array[ offset + 3 ] = te[ 3 ];
       array[ offset + 4 ] = te[ 4 ];
       array[ offset + 5 ] = te[ 5 ];

       array[ offset + 6 ] = te[ 6 ];
       array[ offset + 7 ] = te[ 7 ];
       array[ offset + 8 ] = te[ 8 ];

       return array;

    }

    public float[] toArray(final float[] array)
    {
        final int offset=0;

        final float[] te = this.elements;

        array[ offset ] = te[ 0 ];
        array[ offset + 1 ] = te[ 1 ];
        array[ offset + 2 ] = te[ 2 ];

        array[ offset + 3 ] = te[ 3 ];
        array[ offset + 4 ] = te[ 4 ];
        array[ offset + 5 ] = te[ 5 ];

        array[ offset + 6 ] = te[ 6 ];
        array[ offset + 7 ] = te[ 7 ];
        array[ offset + 8 ] = te[ 8 ];

        return array;
    }

    public float[] toArray(final float[] array, final int offset)
    {
        final float[] te = this.elements;

        array[ offset ] = te[ 0 ];
        array[ offset + 1 ] = te[ 1 ];
        array[ offset + 2 ] = te[ 2 ];

        array[ offset + 3 ] = te[ 3 ];
        array[ offset + 4 ] = te[ 4 ];
        array[ offset + 5 ] = te[ 5 ];

        array[ offset + 6 ] = te[ 6 ];
        array[ offset + 7 ] = te[ 7 ];
        array[ offset + 8 ] = te[ 8 ];

        return array;
    }

    public Matrix3 transpose()
    {
        float tmp;
        final float[] m = this.elements;

        tmp = m[ 1 ]; m[ 1 ] = m[ 3 ]; m[ 3 ] = tmp;
        tmp = m[ 2 ]; m[ 2 ] = m[ 6 ]; m[ 6 ] = tmp;
        tmp = m[ 5 ]; m[ 5 ] = m[ 7 ]; m[ 7 ] = tmp;
        return this;
    }

    public void transposeIntoArray(final float[] r)
    {
        final float[] m = this.elements;

        r[ 0 ] = m[ 0 ];
        r[ 1 ] = m[ 3 ];
        r[ 2 ] = m[ 6 ];
        r[ 3 ] = m[ 1 ];
        r[ 4 ] = m[ 4 ];
        r[ 5 ] = m[ 7 ];
        r[ 6 ] = m[ 2 ];
        r[ 7 ] = m[ 5 ];
        r[ 8 ] = m[ 8 ];
    }

}
