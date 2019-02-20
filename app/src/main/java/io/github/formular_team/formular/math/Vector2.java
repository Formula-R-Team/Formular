package io.github.formular_team.formular.math;

import java.math.*;

public class Vector2 {

    private float x;
    private float y;

    public Vector2()
    {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public boolean isVector2()
    {
        return true;
    }

    public float height()
    {
        return y;
    }

    public float y()
    {
        return y;
    }

    public float width()
    {
        return x;
    }

    public float x()
    {
        return x;
    }

    public void add(Vector2 v)
    {
        this.x += v.width();
        this.y += v.height();
    }

    public void addScalar(Float s)
    {
        this.x += s;
        this.y += s;
    }

    public void addScaledVector(Vector2 v, float s)
    {
        this.x += v.width() * s;
        this.y += v.height() * s;
    }

    public void addVectors(Vector2 a, Vector2 b)
    {
        this.x = a.width() + b.width();
        this.y = a.height() + b.height();
    }

    public float angle()
    {
        float angle = Mth.atan2(this.y, this.x);

        if(angle<0)
            angle+= 2* Math.PI;

        return angle;
    }


    public void applyMatrix3(Matrix3 m)
    {
        float x= this.x;
        float y = this.y;

        float[] e = m.elements();

        this.x = e[0] * x + e[3] * y + e[6];
        this.y = e[1] * x + e[4] * y + e[7];
    }

    public void ceil()
    {
        this.x = Mth.ceil(this.x);
        this.y = Mth.ceil(this.y);
    }

    public void clamp(Vector2 min, Vector2 max)
    {
        this.x = Math.max( min.width(), Math.min( max.width(), this.width() ) );
        this.y = Math.max( min.height(), Math.min( max.height(), this.height() ) );
    }

    public void clampLength(float min, float max)
    {

        float length = this.length();

        this.divideScalar(length);
        this.multiplyScalar(Math.max(min, Math.min(max, length)));
    }

    public void clampScalar(float min, float max)
    {
        Vector2 minV = new Vector2(min,min);
        Vector2 maxV = new Vector2(max,max);

        this.clamp(minV, maxV);
    }

    public Vector2 clone()
    {
        return new Vector2(this.x, this.y);
    }

    public void copy(Vector2 that)
    {
        this.x = that.width();
        this.y = that.height();
    }

    public float distanceTo(Vector2 v)
    {

        return Mth.sqrt( this.distanceToSquared( v ) );
    }

    public float manhattanDistanceTo(Vector2 v)
    {
        return Math.abs( this.x - v.width() ) + Math.abs( this.y - v.height() );
    }

    public float distanceToSquared(Vector2 v)
    {
        float dx = this.x - v.width();
        float dy = this.y - v.height();
        return dx * dx + dy * dy;
    }

    public void divide(Vector2 v)
    {

        if(v.width() !=0.0f)
            this.x /= v.width();
        if(v.height() !=0.0f)
            this.y /= v.height();
    }

    public void divideScalar(float s)
    {
        this.multiplyScalar( 1 / s );
    }

    public float dot(Vector2 v)
    {
        return this.x * v.width() + this.y * v.height();
    }

    public float cross(Vector2 v)
    {
        return this.x * v.height() - this.y * v.width();
    }

    public boolean equals(Vector2 that)
    {
        if (BigDecimal.valueOf(this.x).equals(BigDecimal.valueOf(that.width())))
            return BigDecimal.valueOf(this.y).equals(BigDecimal.valueOf(that.height()));
        return false;
    }

    public void floor()
    {
        this.x = Mth.floor(this.x);
        this.y = Mth.floor(this.y);
    }

    public void fromArray(float[] array, int offset)
    {
        this.x = array[offset];
        this.y = array[offset+1];
    }

    public void fromArray(float[] array)
    {
        this.x = array[0];
        this.y = array[1];
    }


    public float getComponent(int index)
    {
        switch ( index ) {

            case 0: return this.x;
            case 1: return this.y;
            default: throw new Error( "index is out of range: " + index );

        }
    }

    public float length()
    {
        return Mth.sqrt( this.x * this.x + this.y * this.y );
    }

    public float manhattanLength()
    {
        return Math.abs( this.x ) + Math.abs( this.y );
    }

    public float lengthSq()
    {
        return this.x * this.x + this.y * this.y;
    }

    public void lerp(Vector2 v, float alpha)
    {
        this.x += ( v.width() - this.x ) * alpha;
        this.y += ( v.height() - this.y ) * alpha;
    }

    public void lerpVectors(Vector2 v1, Vector2 v2, float alpha)
    {
        this.subVectors( v2, v1 );
        this.multiplyScalar( alpha );
        this.add( v1 );
    }

    public void negate()
    {
        this.x = -x;
        this.y = -y;
    }

    public void normalize()
    {
        this.divideScalar( this.length());
    }

    public void max(Vector2 v)
    {
        if(this.x < v.width())
            this.x = v.width();
        if(this.y < v.height())
            this.y = v.height();
    }

    public void min(Vector2 v)
    {
        if(this.x > v.width())
            this.x = v.width();
        if(this.y > v.height())
            this.y = v.height();
    }

    public void multiply(Vector2 v)
    {
        this.x *= v.width();
        this.y *= v.height();
    }

    public void multiplyScalar(float s)
    {
        this.x *= s;
        this.y *= s;
    }

    public void rotateAround(Vector2 center, float angle)
    {
        float c = Mth.cos( angle );
        float s = Mth.sin( angle );

        float x = this.x - center.width();
        float y = this.y - center.height();

        this.x = x * c - y * s + center.width();
        this.y = x * s + y * c + center.height();
    }

    public void round()
    {
        this.x = Math.round( this.x );
        this.y = Math.round( this.y );
    }

    public void roundToZero()
    {
        if(this.x <0.0f)
            this.x = Mth.ceil(this.x);
        else
            this.x = Mth.floor(this.x);

        if(this.y <0.0f)
            this.y = Mth.ceil(this.y);
        else
            this.y = Mth.floor(this.y);
    }

    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void  setComponent(int index, float value)
    {
        switch ( index ) {

            case 0: this.x = value; break;
            case 1: this.y = value; break;
            default: throw new Error( "index is out of range: " + index );

        }
    }

    public void setLength(float l)
    {
        this.normalize();
        this.multiplyScalar( l );
    }

    public void setScalar(float scalar)
    {
        this.x = scalar;
        this.y = scalar;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public void sub(Vector2 v)
    {
        this.x -= v.width();
        this.y -= v.height();
    }

    public void subScalar(float s)
    {
        this.x -= s;
        this.y -=s;
    }

    public void subVectors(Vector2 a, Vector2 b)
    {
        this.x = a.width() - b.width();
        this.y = a.height() - b.height();
    }


    public float[] toArray()
    {
        return  new float[]{this.x, this.y};
    }

    public float[] toArray(float[] array)
    {
        return this.toArray(array, 0);
    }

    public float[] toArray(float[] array, int offset)
    {
        array[offset] = this.x;
        array[offset+1] = this.y;
        return array;
    }


}
