package io.github.formular_team.formular.math;

import java.util.LinkedList;

import static io.github.formular_team.formular.math.Interpolations.CubicBezier;
//TODO Please check especially any method that uses divisions and the getUtoTmapping
//some confusion because of swapping from weakly typed language to strongly typed
public final class CubicBezierCurve implements Curve {

    private Vector2 v0;
    private Vector2 v1;
    private Vector2 v2;
    private Vector2 v3;


    public CubicBezierCurve(final Vector2 v0, final Vector2 v1, final Vector2 v2, final Vector2 v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;

    }
    public CubicBezierCurve() {
        this.v0 = new Vector2();
        this.v1 = new Vector2();
        this.v2 = new Vector2();
        this.v3 = new Vector2();
    }

    public Vector2 v0() {return v0;}
    public Vector2 v1() {return v1;}
    public Vector2 v2() {return v2;}
    public Vector2 v3() {return v3;}

    @Override
    public Vector2 getPoint(final float t) {

        Vector2 point = new Vector2();

        Vector2 v0 = this.v0, v1 = this.v1, v2 = this.v2, v3 = this.v3;

        point.set(
                CubicBezier( t, v0.x(), v1.x(), v2.x(), v3.x() ),
                CubicBezier( t, v0.y(), v1.y(), v2.y(), v3.y() )
        );

        return point;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        float t = this.getUtoTmapping( u );
        return this.getPoint( t );
    }

    @Override
    public Vector2[] getPoints(final int divisions) {
        LinkedList<Vector2> points = new LinkedList<>();

        for ( int d = 0; d <= divisions; d ++ ) {

            points.push( this.getPoint( d / divisions ) );

        }

        return points.toArray(new Vector2[0]);
    }

    @Override
    public Vector2[] getSpacedPoints(final int divisions) {
        LinkedList<Vector2> points = new LinkedList<>();

        for ( int d = 0; d <= divisions; d ++ ) {

            points.push( this.getPoint( d / divisions ) );

        }

        return points.toArray(new Vector2[0]);
    }

    @Override
    public float getLength() {

        float[] lengths =this.getLengths();
        return lengths[lengths.length-1];
    }

    @Override
    public float[] getLengths(final int divisions) {
        LinkedList<Float> cache = new LinkedList<>();
        Vector2 current, last = this.getPoint(0);
        float p, sum=0;

        for(p=1; p<=divisions; p++)
        {
            current = this.getPoint(p/divisions);
            sum+= current.distanceTo(last);
            cache.push(sum);
            last=current;
        }
        Float[] array = new Float[0];
        array = cache.toArray(array);
        float[] temp = new float[array.length-1];
        for(int i=0;i<array.length-1;i++)
            temp[i] = array[i];

        return temp;
    }



    public float[] getLengths() {
        int divisions = 200;

        LinkedList<Float> cache = new LinkedList<>();
        Vector2 current, last = this.getPoint(0);
        float p, sum=0;

        for(p=1; p<=divisions; p++)
        {
            current = this.getPoint(p/divisions);
            sum+= current.distanceTo(last);
            cache.push(sum);
            last=current;
        }
        Float[] array = new Float[0];
        array = cache.toArray(array);
        float[] temp = new float[array.length-1];
        for(int i=0;i<array.length-1;i++)
            temp[i] = array[i];

        return temp;
    }

    @Override
    public Vector2 getTangent(final float t) {
        float delta = 0.0001f;
        float t1 = t - delta;
        float t2 = t + delta;

        // Capping in case of danger

        if ( t1 < 0 ) t1 = 0f;
        if ( t2 > 1 ) t2 = 1f;

        Vector2 pt1 = this.getPoint( t1 );
        Vector2 pt2 = this.getPoint( t2 );

        Vector2 vec = pt2.copy();
        vec.sub( pt1 );
        vec.normalize();
        return vec;
    }

    @Override
    public Vector2 getTangentAt(final float u) {
        float t = this.getUtoTmapping(u);
        return this.getTangent(t);
    }

    @Override
    public Curve copy() {
        CubicBezierCurve toReturn = new CubicBezierCurve();
        toReturn.v0().copy(this.v0);
        toReturn.v1().copy(this.v1);
        toReturn.v2().copy(this.v2);
        toReturn.v3().copy(this.v3);

        return toReturn;
    }

    public void copy(CubicBezierCurve in) {
        this.v0.copy(in.v0());
        this.v1.copy(in.v1());
        this.v2.copy(in.v2());
        this.v3.copy(in.v3());
    }

    private float getUtoTmapping(float u) {
        float[] arcLengths = this.getLengths();
        int i, il=arcLengths.length;
        float targetArcLength;

        targetArcLength = u * arcLengths[il-1];

        int low =0, high =il;
        float comparison;

        while(low < high){
            i= (int) Math.floor(low+(high-low)/2f);
            comparison = arcLengths[i]-targetArcLength;

            if ( comparison < 0f ) {

                low = i + 1;

            } else if ( comparison > 0f ) {

                high = i - 1;

            } else {

                high = i;
                break;

                // DONE

            }

        }

        i = high;

        if ( arcLengths[ i ] == targetArcLength ) {

            return i / ( il - 1f );

        }

        // we could get finer grain at lengths, or use simple interpolation between two points

        float lengthBefore = arcLengths[ i ];
        float lengthAfter = arcLengths[ i + 1 ];

        float segmentLength = lengthAfter - lengthBefore;

        // determine where we are between the 'before' and 'after' points

        float segmentFraction = ( targetArcLength - lengthBefore ) / segmentLength;

        // add that fractional amount to t

        return ( i + segmentFraction ) / ( il - 1 );

    }

    public float getUtoTmapping(float u, float distance) {
        float[] arcLengths = this.getLengths();
        int i, il=arcLengths.length;
        float targetArcLength;

        targetArcLength = distance;

        int low =0, high =il;
        float comparison;

        while(low < high){
            i= (int) Math.floor(low+(high-low)/2f);
            comparison = arcLengths[i]-targetArcLength;

            if ( comparison < 0f ) {

                low = i + 1;

            } else if ( comparison > 0f ) {

                high = i - 1;

            } else {

                high = i;
                break;

                // DONE

            }

        }

        i = high;

        if ( arcLengths[ i ] == targetArcLength ) {

            return i / ( il - 1f );

        }

        // we could get finer grain at lengths, or use simple interpolation between two points

        float lengthBefore = arcLengths[ i ];
        float lengthAfter = arcLengths[ i + 1 ];

        float segmentLength = lengthAfter - lengthBefore;

        // determine where we are between the 'before' and 'after' points

        float segmentFraction = ( targetArcLength - lengthBefore ) / segmentLength;

        // add that fractional amount to t

        return ( i + segmentFraction ) / ( il - 1 );
    }
}
