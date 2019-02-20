package io.github.formular_team.formular.math;

public class Vector3
{
    private float x;
    private float y;
    private float z;

    public Vector3()
    {
        this.x=0;
        this.y=0;
        this.z=0;
    }

    public Vector3(float x, float y, float z)
    {
        this.x=x;
        this.y=y;
        this.z=z;
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

    public void add(Vector3 v)
    {
        this.x += v.x();
        this.y += v.y();
        this.z += v.z();
    }

    public void addScalar(float s)
    {
        this.x += s;
        this.y += s;
        this.z += s;
    }

    public void addVectors(Vector3 a, Vector3 b)
    {
        this.x = a.x() + b.x();
        this.y = a.y() + b.y();
        this.z = a.z() + b.z();
    }

    public void applyAxisAngle(Vector3 axis, float angle)
    {
        Quaternion quaternion = new Quaternion();
        quaternion.setFromAxisAngle( axis, angle );
        this.applyQuaternion( quaternion );
    }

    public void applyMatrix3(Matrix3 m)
    {
        float x = this.x, y = this.y, z = this.z;
        float[] e = m.elements();

        this.x = e[ 0 ] * x + e[ 3 ] * y + e[ 6 ] * z;
        this.y = e[ 1 ] * x + e[ 4 ] * y + e[ 7 ] * z;
        this.z = e[ 2 ] * x + e[ 5 ] * y + e[ 8 ] * z;
    }

    public void applyMatrix4(Matrix4 m)
    {
        float x = this.x, y = this.y, z = this.z;
        float[] e = m.elements();

        float w = 1.0f / ( e[ 3 ] * x + e[ 7 ] * y + e[ 11 ] * z + e[ 15 ] );

        this.x = ( e[ 0 ] * x + e[ 4 ] * y + e[ 8 ] * z + e[ 12 ] ) * w;
        this.y = ( e[ 1 ] * x + e[ 5 ] * y + e[ 9 ] * z + e[ 13 ] ) * w;
        this.z = ( e[ 2 ] * x + e[ 6 ] * y + e[ 10 ] * z + e[ 14 ] ) * w;
    }

    public void applyQuaternion(Quaternion q)
    {
        float x = this.x, y = this.y, z = this.z;
        float qx = q.x(), qy = q.y(), qz = q.z(), qw = q.w();

        // calculate quat * vector

        float ix = qw * x + qy * z - qz * y;
        float iy = qw * y + qz * x - qx * z;
        float iz = qw * z + qx * y - qy * x;
        float iw = - qx * x - qy * y - qz * z;

        // calculate result * inverse quat

        this.x = ix * qw + iw * - qx + iy * - qz - iz * - qy;
        this.y = iy * qw + iw * - qy + iz * - qx - ix * - qz;
        this.z = iz * qw + iw * - qz + ix * - qy - iy * - qx;
    }

    public float angleTo(Vector3 v)
    {
        float theta = this.dot( v ) / ( Mth.sqrt( this.lengthSq() * v.lengthSq() ) );

        // clamp, to handle numerical problems

        return Mth.acos( Mth.clamp( theta, - 1.0f, 1.0f ) );
    }

    public void ceil()
    {
        this.x = Mth.ceil( this.x );
        this.y = Mth.ceil( this.y );
        this.z = Mth.ceil( this.z );
    }

    public void clamp(Vector3 min, Vector3 max)
    {
        this.x = Math.max( min.x(), Math.min( max.x(), this.x ) );
        this.y = Math.max( min.y(), Math.min( max.y(), this.y ) );
        this.z = Math.max( min.z(), Math.min( max.z(), this.z ) );
    }

    public void clampLength(float min, float max)
    {
        float length = this.length();

        this.divideScalar( length );
        this.multiplyScalar( Math.max( min, Math.min( max, length ) ) );
    }

    public void clampScalar(float minVal, float maxVal)
    {
        Vector3 min = new Vector3();
        Vector3 max = new Vector3();

        min.set( minVal, minVal, minVal );
        max.set( maxVal, maxVal, maxVal );

        this.clamp( min, max );
    }

    public Vector3 clone()
    {
        return new Vector3( this.x, this.y, this.z );
    }

    public void copy(Vector3 v)
    {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
    }

    public void cross(Vector3 v)
    {
        this.crossVectors( this, v );
    }

    public void crossVectors(Vector3 a, Vector3 b)
    {
        float ax = a.x(), ay = a.y(), az = a.z();
        float bx = b.x(), by = b.y(), bz = b.z();

        this.x = ay * bz - az * by;
        this.y = az * bx - ax * bz;
        this.z = ax * by - ay * bx;
    }

    public float distanceTo(Vector3 v)
    {
        return Mth.sqrt( this.distanceToSquared( v ) );
    }

    public float manhattanDistanceTo(Vector3 v)
    {
        return Math.abs( this.x - v.x() ) + Math.abs( this.y - v.y() ) + Math.abs( this.z - v.z() );
    }

    public float distanceToSquared(Vector3 v)
    {
        float dx = this.x - v.x(), dy = this.y - v.y(), dz = this.z - v.z();

        return dx * dx + dy * dy + dz * dz;
    }

    public void divide(Vector3 v)
    {
        this.x /= v.x();
        this.y /= v.y();
        this.z /= v.z();
    }

    public void divideScalar(float s)
    {
        this.multiplyScalar( 1f / s );
    }

    public float dot(Vector3 v)
    {
        return this.x * v.x() + this.y * v.y() + this.z * v.z();
    }

    public boolean equals(Vector3 v)
    {
        return ( ( v.x() == this.x ) && ( v.y() == this.y ) && ( v.z() == this.z ) );
    }

    public void floor()
    {
        this.x = Mth.floor( this.x );
        this.y = Mth.floor( this.y );
        this.z = Mth.floor( this.z );
    }

    public void fromArray(float[] array)
    {
       fromArray(array, 0);
    }
    public void fromArray(float[] array, int offset)
    {
        this.x = array[ offset ];
        this.y = array[ offset + 1 ];
        this.z = array[ offset + 2 ];
    }

    public float getComponent(int index)
    {
        switch ( index ) {

            case 0: return this.x;
            case 1: return this.y;
            case 2: return this.z;
            default: throw new Error( "index is out of range: " + index );

        }
    }

    public float length()
    {
        return Mth.sqrt( this.x * this.x + this.y * this.y + this.z * this.z );
    }

    public float manhattanLength()
    {
        return Math.abs( this.x ) + Math.abs( this.y ) + Math.abs( this.z );
    }

    public float lengthSq()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public void lerp(Vector3 v, float alpha)
    {
        this.x += ( v.x() - this.x ) * alpha;
        this.y += ( v.y() - this.y ) * alpha;
        this.z += ( v.z() - this.z ) * alpha;
    }

    public void lerpVectors(Vector3 v1, Vector3 v2, float alpha)
    {
        this.subVectors( v2, v1 );
        this.multiplyScalar( alpha );
        this.add(v1);
    }

    public void max(Vector3 v)
    {
        this.x = Math.max( this.x, v.x() );
        this.y = Math.max( this.y, v.y() );
        this.z = Math.max( this.z, v.z() );
    }

    public void min(Vector3 v)
    {
        this.x = Math.min( this.x, v.x() );
        this.y = Math.min( this.y, v.y() );
        this.z = Math.min( this.z, v.z() );
    }

    public void multiply(Vector3 v)
    {
        this.x *= v.x();
        this.y *= v.y();
        this.z *= v.z();
    }

    public void multiplyScalar(float scalar)
    {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public void multiplyVectors(Vector3 a, Vector3 b)
    {
        this.x = a.x() * b.x();
        this.y = a.y() * b.y();
        this.z = a.z() * b.z();
    }

    public void negate()
    {
        this.x= -this.x;
        this.y= -this.y;
        this.z= -this.z;
    }

    public void normalize()
    {
        this.divideScalar( this.length());
    }

    public void projectOnPlane(Vector3 planeNormal)
    {
        Vector3 v1 = new Vector3();
        v1.copy( this );
        v1.projectOnVector( planeNormal );

        this.sub( v1 );
    }

    public void projectOnVector(Vector3 vector)
    {
        float scalar = vector.dot( this ) / vector.lengthSq();

        this.copy( vector );
        this.multiplyScalar( scalar );
    }

    public void reflect(Vector3 normal)
    {
        Vector3 v1 = new Vector3();
        v1.copy( normal );
        v1.multiplyScalar( 2 * this.dot( normal ) );
        this.sub(v1);
    }

    public void round()
    {
        this.x = Math.round( this.x );
        this.y = Math.round( this.y );
        this.z = Math.round( this.z );
    }

    public void roundToZero()
    {

        if(this.x <0.0f)
            this.x =  Mth.ceil(this.x);
        else
            this.x =  Mth.floor(this.x);
        if(this.y <0.0f)
            this.y =  Mth.ceil(this.y);
        else
            this.y =  Mth.floor(this.y);
        if(this.z <0.0f)
            this.z =  Mth.ceil(this.z);
        else
            this.z =  Mth.floor(this.z);
    }

    public void set(float x, float y, float z)
    {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public void setComponent(int index, float value)
    {
        switch ( index ) {

            case 0: this.x = value; break;
            case 1: this.y = value; break;
            case 2: this.z = value; break;
            default: throw new Error( "index is out of range: " + index );

        }
    }

    public void setFromCylindricalCoords(float radius, float theta, float y)
    {
        this.x = (radius * Mth.sin( theta ));
        this.y = y;
        this.z = (radius * Mth.cos( theta ));
    }


    public void setFromMatrixColumn(Matrix4 m, int index)
    {
        this.fromArray( m.elements(), index * 4 );
    }



    public void setFromMatrixPosition(Matrix4 matrix)
    {
        float[] e = matrix.elements();

        this.x = e[ 12 ];
        this.y = e[ 13 ];
        this.z = e[ 14 ];
    }


    public void setFromMatrixScale(Matrix4 m)
    {
        this.setFromMatrixColumn( m, 0 );
        float sx = this.length();
        this.setFromMatrixColumn( m, 1 );
        float sy = this.length();
        this.setFromMatrixColumn( m, 2 );
        float sz = this.length();

        this.x = sx;
        this.y = sy;
        this.z = sz;
    }


    public void setFromSphericalCoords(float radius, float phi, float theta)
    {
        float sinPhiRadius = Mth.sin( phi ) * radius;

        this.x = sinPhiRadius * Mth.sin( theta );
        this.y = Mth.cos( phi ) * radius;
        this.z = sinPhiRadius * Mth.cos( theta );
    }

    public void setLength(float l)
    {
        this.normalize();
        this.multiplyScalar( l );
    }

    public void setScalar(Float scalar)
    {
        this.x=scalar;
        this.y=scalar;
        this.z=scalar;
    }

    public void setX(float x)
    {
        this.x=x;
    }

    public void setY(float y)
    {
        this.y=y;
    }

    public void setZ(float z)
    {
        this.z=z;
    }

    public void sub(Vector3 v)
    {
        this.x -= v.x();
        this.y -= v.y();
        this.z -= v.z();
    }

    public void subScalar(float s)
    {
        this.x-=s;
        this.y-=s;
        this.z-=s;
    }

    public void subVectors(Vector3 a, Vector3 b)
    {
        this.x = a.x() - b.x();
        this.y = a.y() - b.y();
        this.z = a.z() - b.z();
    }

    public float[] toArray()
    {
        return toArray(new float[3], 0);
    }

    public float[] toArray(float[] array)
    {
        return toArray(array, 0);
    }

    public float[] toArray(float[] array, int offset)
    {
        array[ offset ] = this.x;
        array[ offset + 1 ] = this.y;
        array[ offset + 2 ] = this.z;

        return array;
    }


    public void transformDirection(Matrix4 m)
    {
        float x = this.x, y = this.y, z = this.z;
        float[] e = m.elements();

        this.x = e[ 0 ] * x + e[ 4 ] * y + e[ 8 ] * z;
        this.y = e[ 1 ] * x + e[ 5 ] * y + e[ 9 ] * z;
        this.z = e[ 2 ] * x + e[ 6 ] * y + e[ 10 ] * z;

       this.normalize();
    }







}
