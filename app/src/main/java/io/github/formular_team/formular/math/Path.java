package io.github.formular_team.formular.math;

import java.util.ArrayList;
import java.util.LinkedList;

public class Path implements Curve{

    private ArrayList<Curve> curves;

    Path(final Builder builder) {
        this.curves = builder.getCurves();
    }

    //Not sure if this is how visit should look
    public void visit(final PathVisitor visitor) {
        if (!this.curves.isEmpty()) {
            final Curve first = this.curves.get(0);
            visitor.moveTo(first instanceof LineCurve ? ((LineCurve) first).v0() : ((CubicBezierCurve) first).v0());
        }
        for (Curve curCurve:curves) {
            if(curCurve instanceof LineCurve){
                visitor.lineTo(((LineCurve)curCurve).v1());
            }else{
                CubicBezierCurve curBezier = (CubicBezierCurve) curCurve;
                visitor.bezierCurveTo(curBezier.v1(), curBezier.v2(), curBezier.v3());
            }
        }
    }

    public Curve[] getCurves()
    {
        return curves.toArray(new Curve[0]);
    }

    public static Builder builder() {
        return new Builder();
    }


    //TODO taken from CurvePath. Doesn't really make sense to me
    @Override
    public Vector2 getPoint(float t) {
        float d = t * this.getLength();
        float[] curveLengths = this.getCurveLengths();
        int i = 0;

        // To think about boundaries points.

        while ( i < curveLengths.length ) {

            if ( curveLengths[ i ] >= d ) {

                float diff = curveLengths[ i ] - d;
                Curve curve = curves.get(i);

                float segmentLength = curve.getLength();
                float u = segmentLength == 0 ? 0 : 1 - diff / segmentLength;

                return curve.getPointAt( u );

            }

            i++;

        }

        return null;
    }

    @Override
    public Vector2 getPointAt(float u) {
        float t = this.getUtoTmapping( u );
        return this.getPoint( t);
    }

    @Override
    public Vector2[] getPoints(int divisions) {
        ArrayList<Vector2> points = new ArrayList<>();
        Vector2 last = new Vector2();

        for ( int i = 0; i < curves.size(); i ++ ) {

            Curve curve = curves.get(i);
            int resolution = ( ( curve instanceof LineCurve) ) ? 1
                    : divisions;

            Vector2[] pts = curve.getPoints( resolution );

            for (Vector2 point : pts) {

                points.add(0, point);
                last = point;

            }

        }

        return points.toArray(new Vector2[0]);
    }

    public Vector2[] getPoints() {
        return getPoints(12);
    }

    @Override
    public Vector2[] getSpacedPoints(int divisions) {

        ArrayList<Vector2> points = new ArrayList<>();

        for ( int d = 0; d <= divisions; d ++ ) {

            points.add(0, this.getPointAt( d / divisions ) );

        }

        return points.toArray(new Vector2[0]);
    }

    @Override
    public Vector2[] getSpacedPoints() {
        return getSpacedPoints(40);
    }

    @Override
    public float getLength() {
        float[] lens = this.getCurveLengths();
        return lens[ lens.length - 1 ];
    }

    @Override
    public float[] getLengths(int divisions) {
        LinkedList<Float> cache = new LinkedList<>();
        Vector2 current, last = this.getPoint(0f);
        float p, sum=0f;

        for(p=1; p<=divisions; p++)
        {
            current = this.getPoint(p/divisions);
            sum+= current.distanceTo(last);
            cache.push(sum);
            last=current;
        }
        Float[] array = cache.toArray(new Float[0]);
        float[] temp = new float[array.length-1];
        for(int i=0;i<array.length-1;i++)
            temp[i] = array[i];

        return temp;
    }

    public float[] getLengths() {
        return getLengths(200);
    }

    @Override
    public Vector2 getTangent(float t) {
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
    public Vector2 getTangentAt(float u) {
        float t = this.getUtoTmapping(u);
        return this.getTangent(t);
    }

    public float[] getCurveLengths(){


        // Get length of sub-curve
        // Push sums into cached array

        LinkedList<Float> lengths = new LinkedList<>();
        float sums = 0;

        for ( int i = 0, l = this.curves.size(); i < l; i ++ ) {

            sums += this.curves.get(i).getLength();
            lengths.push( sums );

        }
        Float[] array = lengths.toArray(new Float[0]);
        float[] temp = new float[array.length-1];
        for(int i=0;i<array.length-1;i++)
            temp[i] = array[i];

        return temp;
    }

    public void copy(Path path) {
        Curve[] newCurves = path.getCurves();
        curves = new ArrayList<>();

        for (Curve curve : newCurves) {
            curves.add(curve.copy());
        }
    }

    public Curve copy() {

        Builder newBuilder = new Builder();
        for (Curve curCurve:curves) {
            if(curCurve instanceof LineCurve){
                newBuilder.lineTo(((LineCurve)curCurve).v1());
            }else{
                CubicBezierCurve curBezier = (CubicBezierCurve) curCurve;
                newBuilder.bezierCurveTo(curBezier.v1(), curBezier.v2(), curBezier.v3());
            }
        }
        return newBuilder.build();
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

    public static class Builder implements PathVisitor {

        private Vector2 currentPoint;
        private ArrayList<Curve> curves;


        Builder() {
            this.currentPoint = new Vector2();
            this.curves = new ArrayList<>();
        }

        public Builder(Vector2[] points)
        {
            setFromPoints(points);
        }

        private Builder setFromPoints(Vector2[] points)
        {
            this.moveTo(points[0]);

            for(int i =1; i < points.length;i++){
                this.lineTo(points[i]);
            }

            return this;
        }

        public ArrayList<Curve> getCurves(){
            return curves;
        }

        @Override
        public Builder moveTo(final Vector2 point) {

            this.currentPoint = point.copy();
            return this;
        }


        @Override
        public Builder lineTo(final Vector2 point)
        {
            LineCurve curve = new LineCurve(currentPoint.copy(),point.copy());
            curves.add(curve);
            this.currentPoint = point.copy();
            return this;
        }
        @Override
        public Builder bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
            CubicBezierCurve curve = new CubicBezierCurve(currentPoint.copy(), controlA.copy(), controlB.copy(), point.copy());
            curves.add(curve);
            this.currentPoint = point.copy();
            return this;
        }
        @Override
        public Builder closePath() {
            return this.lineTo(curves.get(0).getPoint(0).copy());
        }

        public Path build() {
            return new Path(this);
        }
    }
}
