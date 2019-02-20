package io.github.formular_team.formular.math;

public class Quaternion
{
    private float x, y, z, w;

    public Quaternion(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float w() {
        return w;
    }

    public float angleTo(Quaternion q)
    {
        return 2f * Mth.acos( Math.abs( Mth.clamp( this.dot( q ), - 1, 1 ) ) );
    }

    public Quaternion clone()
    {
        return new Quaternion(x,y,z,w);
    }

    public Quaternion conjugate()
    {
        this.x *= - 1.0f;
        this.y *= - 1.0f;
        this.z *= - 1.0f;
        return null;
    }

    public void copy(Quaternion q)
    {
        this.x = q.w();
        this.y = q.y();
        this.z = q.z();
        this.w = q.w();
    }

    public boolean equals(Quaternion q)
    {
        return w == q.w() && x == q.x() && y == q.y() && z == q.z();
    }

    public float dot(Quaternion v)
    {
        return this.x * v.x() + this.y * v.y() + this.z * v.z() + this.w * v.w();
    }

    public void fromArray(float[] array)
    {
        this.fromArray(array, 0);
    }

    public void fromArray(float[] array, int offset)
    {
        this.x = array[ offset ];
        this.y = array[ offset + 1 ];
        this.z = array[ offset + 2 ];
        this.w = array[ offset + 3 ];
    }

    public void inverse()
    {
        this.conjugate();
    }

    public float length()
    {
        return Mth.sqrt( this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w );
    }

    public float lengthSq()
    {
       return ( this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w );
    }

    public void normalize()
    {
        float l = this.length();

        if ( l == 0 ) {

            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.w = 1;

        } else {

            l = 1 / l;

            this.x = this.x * l;
            this.y = this.y * l;
            this.z = this.z * l;
            this.w = this.w * l;

        }
    }

    public void multiply(Quaternion q)
    {
        this.multiplyQuaternions(this,q);
    }

    public void multiplyQuaternions(Quaternion a, Quaternion b)
    {
        float qax = a.x(), qay = a.y(), qaz = a.z(), qaw = a.w();
        float qbx = b.x(), qby = b.y(), qbz = b.z(), qbw = b.w();

        this.x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby;
        this.y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz;
        this.z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx;
        this.w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz;
    }

    public void premultiply(Quaternion q)
    {
        this.multiplyQuaternions(q,this);
    }

    public void rotateTowards(Quaternion q, float step)
    {
        float angle = this.angleTo( q );

        if ( angle == 0.0f ) return;

        float t = Math.min( 1, step / angle );

        this.slerp( q, t );
    }

    public void slerp(Quaternion qb, float t)
    {
        if ( t == 0 ) return;
        if ( t == 1 )
        {
            this.copy( qb );
            return;
        }

        float x = this.x, y = this.y, z = this.z, w = this.w;


        float cosHalfTheta = w * qb.w() + x * qb.x() + y * qb.y() + z * qb.z();

        if ( cosHalfTheta < 0 ) {

            this.w = - qb.w();
            this.x = - qb.x();
            this.y = - qb.y();
            this.z = - qb.z();

            cosHalfTheta = - cosHalfTheta;

        } else {

            this.copy( qb );

        }

        if ( cosHalfTheta >= 1.0 ) {

            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;

        }

        float sqrSinHalfTheta =  1.0f - cosHalfTheta * cosHalfTheta;

        if ( sqrSinHalfTheta <=  Math.ulp(1.0f)) {

            float s = 1 - t;
            this.w = s * w + t * this.w;
            this.x = s * x + t * this.x;
            this.y = s * y + t * this.y;
            this.z = s * z + t * this.z;

            this.normalize();

        }

        float sinHalfTheta = Mth.sqrt( sqrSinHalfTheta );
        float halfTheta =  Mth.atan2( sinHalfTheta, cosHalfTheta );
        float ratioA = Mth.sin( ( 1 - t ) * halfTheta ) / sinHalfTheta,
                ratioB = Mth.sin( t * halfTheta ) / sinHalfTheta;

        this.w = ( w * ratioA + this.w * ratioB );
        this.x = ( x * ratioA + this.x * ratioB );
        this.y = ( y * ratioA + this.y * ratioB );
        this.z = ( z * ratioA + this.z * ratioB );
    }

    public void set(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void setFromAxisAngle(Vector3 axis, float angle)
    {
        float halfAngle = (angle / 2.0f), s  = Mth.sin( halfAngle );

        this.x = axis.x() * s;
        this.y = axis.y() * s;
        this.z = axis.z() * s;
        this.w = Mth.cos( halfAngle );
    }

    public void setFromRotationMatrix(Matrix4 m)
    {
        float[] te = m.elements();

         float  m11 = te[ 0 ], m12 = te[ 4 ], m13 = te[ 8 ],
                m21 = te[ 1 ], m22 = te[ 5 ], m23 = te[ 9 ],
                m31 = te[ 2 ], m32 = te[ 6 ], m33 = te[ 10 ],

                trace = m11 + m22 + m33,
                s;

        if ( trace > 0 ) {

            s = 0.5f / Mth.sqrt( trace + 1.0f );

            this.w = 0.25f / s;
            this.x = ( m32 - m23 ) * s;
            this.y = ( m13 - m31 ) * s;
            this.z = ( m21 - m12 ) * s;

        } else if ( m11 > m22 && m11 > m33 ) {

            s = 2.0f * Mth.sqrt( 1.0f + m11 - m22 - m33 );

            this.w = ( m32 - m23 ) / s;
            this.x =  0.25f * s;
            this.y = ( m12 + m21 ) / s;
            this.z = ( m13 + m31 ) / s;

        } else if ( m22 > m33 ) {

            s = 2.0f * Mth.sqrt( 1.0f + m22 - m11 - m33 );

            this.w = ( m13 - m31 ) / s;
            this.x = ( m12 + m21 ) / s;
            this.y = 0.25f * s;
            this.z = ( m23 + m32 ) / s;

        } else {

            s = 2.0f * Mth.sqrt( 1.0f + m33 - m11 - m22 );

            this.w = ( m21 - m12 ) / s;
            this.x = ( m13 + m31 ) / s;
            this.y = ( m23 + m32 ) / s;
            this.z = 0.25f * s;

        }
    }

    public void setFromUnitVectors(Vector3 vFrom, Vector3 vTo)
    {
        Vector3 v1 = new Vector3();
        float r;

        float EPS = 0.000001f;

        r = vFrom.dot( vTo ) + 1;

        if ( r < EPS ) {

            r = 0;

            if ( Math.abs( vFrom.x() ) > Math.abs( vFrom.z() ) ) {

                v1.set( - vFrom.y(), vFrom.x(), 0.0f );

            } else {

                v1.set( 0.0f, - vFrom.z(), vFrom.y() );

            }

        } else {

            v1.crossVectors( vFrom, vTo );

        }

        this.x = v1.x();
        this.y = v1.y();
        this.z = v1.z();
        this.w = r;
    }

    public float[] toArray()
    {
        return toArray(new float[4], 0);
    }

    public float[] toArray(float[] array)
    {
        return toArray(array, 0);
    }

    public float[] toArray(float[] array, int offset)
    {
        array[ offset ] = this.x();
        array[ offset + 1 ] = this.y();
        array[ offset + 2 ] = this.z();
        array[ offset + 3 ] = this.w();

        return array;
    }

    public static void slerp(Quaternion qa, Quaternion qb, Quaternion qm, float t)
    {
        qm.copy( qa );
        qm.slerp( qb, t );
    }

    public static void slerpFlat(float[] dst, int dstOffset, float[] src0, int srcOffset0, float[] src1, int srcOffset1, float t )
    {
        float x0 = src0[ srcOffset0],
                y0 = src0[ srcOffset0 + 1 ],
                z0 = src0[ srcOffset0 + 2 ],
                w0 = src0[ srcOffset0 + 3 ],

                x1 = src1[ srcOffset1],
                y1 = src1[ srcOffset1 + 1 ],
                z1 = src1[ srcOffset1 + 2 ],
                w1 = src1[ srcOffset1 + 3 ];

        if ( w0 != w1 || x0 != x1 || y0 != y1 || z0 != z1 ) {

            float s = 1f - t,

                    cos = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1,

                    dir = ( cos >= 0 ? 1 : - 1 ),
                    sqrSin = 1 - cos * cos;

            // Skip the Slerp for tiny steps to avoid numeric problems:
            if ( sqrSin > Math.ulp(1.0f) ) {

                float sin = Mth.sqrt( sqrSin ),
                        len = Mth.atan2( sin, cos * dir );

                s = Mth.sin( s * len ) / sin;
                t = Mth.sin( t * len ) / sin;

            }

            float tDir = t * dir;

            x0 = x0 * s + x1 * tDir;
            y0 = y0 * s + y1 * tDir;
            z0 = z0 * s + z1 * tDir;
            w0 = w0 * s + w1 * tDir;

            // Normalize in case we just did a lerp:
            if ( s == 1 - t ) {

                float f = 1.0f / Mth.sqrt( x0 * x0 + y0 * y0 + z0 * z0 + w0 * w0 );

                x0 *= f;
                y0 *= f;
                z0 *= f;
                w0 *= f;

            }

        }

        dst[ dstOffset ] = x0;
        dst[ dstOffset + 1 ] = y0;
        dst[ dstOffset + 2 ] = z0;
        dst[ dstOffset + 3 ] = w0;
    }


}
