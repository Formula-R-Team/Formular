package io.github.formular_team.formular.math;

import java.util.LinkedList;
//TODO Please check especially any method that uses divisions and the getUtoTmapping
//some confusion because of swapping from weakly typed language to strongly typed
public final class LineCurve implements Curve {
    private Vector2 v0;
    private Vector2 v1;

    public LineCurve(final Vector2 v0, final Vector2 v1) {
        this.v0 = v0;
        this.v1 = v1;
    }

    public LineCurve() {
        this.v0 = new Vector2();
        this.v1 = new Vector2();
    }

    public Vector2 v0() {return v0;}
    public Vector2 v1() {return v1;}

    @Override
    public Vector2 getPoint(final float t) {
        Vector2 point = new Vector2();

        if ( t == 1 ) {
            point.copy( this.v1);
        } else {
            point.copy( this.v1);
            point.sub( this.v0);
            point.multiplyScalar( t );
            point.add( this.v0);
        }

        return point;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        return this.getPoint( u );
    }

    @Override
    public Vector2[] getPoints(final int divisions) {
        LinkedList<Vector2> points = new LinkedList<>();

        for ( int d = 0; d <= divisions; d ++ ) {

            points.push( this.getPoint( (float) d / divisions ) );

        }

        return points.toArray(new Vector2[0]);
    }

    @Override
    public Vector2[] getPoints() {
        return getPoints(5);
    }

    @Override
    public Vector2[] getSpacedPoints(final int divisions) {

        LinkedList<Vector2> points = new LinkedList<>();

        for ( int d = 0; d <= divisions; d ++ ) {

            points.push( this.getPoint( (float) d / divisions ) );

        }

        return points.toArray(new Vector2[0]);
    }

    @Override
    public Vector2[] getSpacedPoints() {
        return getSpacedPoints(5);
    }

    @Override
    public float getLength() {
        return this.v0.distanceTo(this.v1);
    }

    @Override
    public float[] getLengths(final int divisions) {
        final float[] lengths = new float[divisions];
        final float length = this.getLength();
        for (int i = 0; i <= divisions; i++) {
            lengths[i] = length * i / divisions;
        }
        return lengths;
    }

    @Override
    public Vector2 getTangent(final float t) {
        Vector2 tangent = this.v1.copy();
        tangent.sub(this.v0);
        tangent.normalize();
        return tangent;
    }

    @Override
    public Vector2 getTangentAt(final float u) {
        float t = this.getUtoTmapping(u);
        return this.getTangent(t);
    }

    @Override
    public Curve copy() {
        return new LineCurve(this.v0.copy(),this.v1.copy());
    }

    public void copy(LineCurve other)
    {
        this.v0 = other.v0().copy();
        this.v1 = other.v1().copy();
    }

    private float getUtoTmapping(final float u) {
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

        return ( i + segmentFraction ) / ( il - 1f );

    }

    private float getUtoTmapping(float u, float distance) {
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
