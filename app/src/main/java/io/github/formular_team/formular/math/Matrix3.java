package io.github.formular_team.formular.math;

public class Matrix3
{
    private float[] elements;

    public Matrix3()
    {
        elements = new float[]{1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f};
    }

    public float[] elements()
    {
        return elements;
    }

    public boolean isMatrix3()
    {
        return true;
    }

    public void set(float n11, float n12, float n13, float n21, float n22, float n23, float n31, float n32, float n33)
    {
        elements[0] = n11;
        elements[1] = n12;
        elements[2] = n13;
        elements[3] = n21;
        elements[4] = n22;
        elements[5] = n23;
        elements[6] = n31;
        elements[7] = n32;
        elements[8] = n33;
    }

    public Matrix3 clone()
    {
        Matrix3 temp = new Matrix3();
        temp.fromArray(this.elements);
        return temp;
    }

    public void copy(Matrix3 m)
    {
        float[] te = this.elements;
        float[] me = m.elements();

        te[ 0 ] = me[ 0 ]; te[ 1 ] = me[ 1 ]; te[ 2 ] = me[ 2 ];
        te[ 3 ] = me[ 3 ]; te[ 4 ] = me[ 4 ]; te[ 5 ] = me[ 5 ];
        te[ 6 ] = me[ 6 ]; te[ 7 ] = me[ 7 ]; te[ 8 ] = me[ 8 ];
    }

    public float determinant()
    {
        float[] te= this.elements;

        float a = te[ 0 ], b = te[ 1 ], c = te[ 2 ],
                d = te[ 3 ], e = te[ 4 ], f = te[ 5 ],
                g = te[ 6 ], h = te[ 7 ], i = te[ 8 ];

        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g;
    }

    public boolean equals(Matrix3 m)
    {
        return elements[0] == m.elements()[0] && elements[1] == m.elements()[1] && elements[2] == m.elements()[2] &&
                elements[3] == m.elements()[3] && elements[4] == m.elements()[4] && elements[5] == m.elements()[5] &&
                elements[6] == m.elements()[6] && elements[7] == m.elements()[7] && elements[8] == m.elements()[8];
    }

    public void fromArray(float[] array)
    {
        System.arraycopy(array, 0, this.elements, 0, 9);
    }

    public void fromArray(float[] array, int offset)
    {
        System.arraycopy(array, offset, this.elements, 0, 9);
    }

    public void getInverse(Matrix3 m)
    {
        float[] me = m.elements();
        float[] te = this.elements;

         float  n11 = me[ 0 ], n21 = me[ 1 ], n31 = me[ 2 ],
                n12 = me[ 3 ], n22 = me[ 4 ], n32 = me[ 5 ],
                n13 = me[ 6 ], n23 = me[ 7 ], n33 = me[ 8 ],

                t11 = n33 * n22 - n32 * n23,
                t12 = n32 * n13 - n33 * n12,
                t13 = n23 * n12 - n22 * n13,

                det = n11 * t11 + n21 * t12 + n31 * t13;

        if ( det == 0 ) {

            this.identity();

        }

        float detInv = 1 / det;

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

    public void getInverse(Matrix3 m, boolean throwOnDegenerate)
    {
        float[] me = m.elements(),
                te = this.elements;

         float  n11 = me[ 0 ], n21 = me[ 1 ], n31 = me[ 2 ],
                n12 = me[ 3 ], n22 = me[ 4 ], n32 = me[ 5 ],
                n13 = me[ 6 ], n23 = me[ 7 ], n33 = me[ 8 ],

                t11 = n33 * n22 - n32 * n23,
                t12 = n32 * n13 - n33 * n12,
                t13 = n23 * n12 - n22 * n13,

                det = n11 * t11 + n21 * t12 + n31 * t13;

        if ( det == 0 ) {

            String msg = "THREE.Matrix3: .getInverse() can't invert matrix, determinant is 0";

            if (throwOnDegenerate) {

                throw new Error( msg );

            }

            this.identity();

        }

        float detInv = 1 / det;

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

    public void getNormalMatrix(Matrix4 m)
    {
        //Unsure of order of operations
        //Original line: this.setFromMatrix4( m ).getInverse( this ).transpose();

        this.setFromMatrix4(m);
        this.getInverse(this);
        this.transpose();

    }

    public void setFromMatrix4( Matrix4 m ) {

        float[] me = m.elements();

        this.set(

                me[0], me[4], me[8],
                me[1], me[5], me[9],
                me[2], me[6], me[10]

        );
    }

    public void identity()
    {
        elements = new float[]{1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f};
    }

    public void multiply(Matrix3 m)
    {
        this.multiplyMatrices( this, m );
    }

    public void multiplyMatrices(Matrix3 a, Matrix3 b)
    {
        float[] ae = a.elements();
        float[] be = b.elements();
        float[] te = this.elements();

        float a11 = ae[ 0 ], a12 = ae[ 3 ], a13 = ae[ 6 ];
        float a21 = ae[ 1 ], a22 = ae[ 4 ], a23 = ae[ 7 ];
        float a31 = ae[ 2 ], a32 = ae[ 5 ], a33 = ae[ 8 ];

        float b11 = be[ 0 ], b12 = be[ 3 ], b13 = be[ 6 ];
        float b21 = be[ 1 ], b22 = be[ 4 ], b23 = be[ 7 ];
        float b31 = be[ 2 ], b32 = be[ 5 ], b33 = be[ 8 ];

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

    public void multiplyScalar(float s)
    {
        float[] te = this.elements;

        te[ 0 ] *= s; te[ 3 ] *= s; te[ 6 ] *= s;
        te[ 1 ] *= s; te[ 4 ] *= s; te[ 7 ] *= s;
        te[ 2 ] *= s; te[ 5 ] *= s; te[ 8 ] *= s;
    }

    public void premultiply(Matrix3 m)
    {
        this.multiplyMatrices( m, this );
    }

    public void setUvTransform(float tx, float ty, float sx, float sy, float rotation, float cx, float cy)
    {
        float c =  Mth.cos( rotation );
        float s =  Mth.sin( rotation );

        this.set(
                sx * c, sx * s, - sx * ( c * cx + s * cy ) + cx + tx,
                - sy * s, sy * c, - sy * ( - s * cx + c * cy ) + cy + ty,
                0, 0, 1
        );
    }

    public float[] toArray()
    {
       float[] array = new float[9];
       int offset=0;

       float[] te = this.elements;

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

    public float[] toArray(float[] array)
    {
        int offset=0;

        float[] te = this.elements;

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

    public float[] toArray(float[] array, int offset)
    {
        float[] te = this.elements;

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

    public void transpose()
    {
        float tmp;
        float[] m = this.elements;

        tmp = m[ 1 ]; m[ 1 ] = m[ 3 ]; m[ 3 ] = tmp;
        tmp = m[ 2 ]; m[ 2 ] = m[ 6 ]; m[ 6 ] = tmp;
        tmp = m[ 5 ]; m[ 5 ] = m[ 7 ]; m[ 7 ] = tmp;
    }

    public void transposeIntoArray(float[] r)
    {
        float[] m = this.elements;

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
