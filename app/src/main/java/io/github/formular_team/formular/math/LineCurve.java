package io.github.formular_team.formular.math;

import com.google.common.collect.Lists;

import java.util.List;

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

    public Vector2 v0() {
        return this.v0;
    }

    public Vector2 v1() {
        return this.v1;
    }

    @Override
    public boolean isStraight() {
        return true;
    }

    @Override
    public Vector2 getPoint(final float t) {
        final Vector2 point = this.v0.copy();
        if (t == 1.0F) {
            point.copy(this.v1);
        } else if (t != 0.0F) {
            point.copy(this.v1);
            point.sub(this.v0);
            point.multiply(t);
            point.add(this.v0);
        }
        return point;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        return this.getPoint(u);
    }

    @Override
    public List<Vector2> getPoints(final int divisions) {
        List<Vector2> points = Lists.newArrayListWithCapacity(1 + divisions);
        for (int d = 0; d <= divisions; d++) {
            points.add(this.getPoint((float) d / divisions));
        }
        return points;
    }

    @Override
    public List<Vector2> getPoints() {
        return this.getPoints(5);
    }

    @Override
    public Vector2[] getSpacedPoints(final int divisions) {
        final Vector2[] points = new Vector2[1 + divisions];
        for (int d = 0; d <= divisions; d++) {
            points[d] = this.getPointAt((float) d / divisions);
        }
        return points;
    }

    @Override
    public Vector2[] getSpacedPoints() {
        return this.getSpacedPoints(5);
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
        return new LineCurve(this.v0.copy(), this.v1.copy());
    }

    public void copy(LineCurve other) {
        this.v0 = other.v0().copy();
        this.v1 = other.v1().copy();
    }

    private float getUtoTmapping(final float u) {
        float[] arcLengths = this.getLengths();
        int i, il = arcLengths.length;
        float targetArcLength;

        targetArcLength = u * arcLengths[il - 1];

        int low = 0, high = il;
        float comparison;

        while (low < high) {
            i = (int) Math.floor(low + (high - low) / 2f);
            comparison = arcLengths[i] - targetArcLength;

            if (comparison < 0f) {

                low = i + 1;

            } else if (comparison > 0f) {

                high = i - 1;

            } else {

                high = i;
                break;

                // DONE

            }

        }

        i = high;

        if (arcLengths[i] == targetArcLength) {

            return i / (il - 1f);

        }

        // we could get finer grain at lengths, or use simple interpolation between two points

        float lengthBefore = arcLengths[i];
        float lengthAfter = arcLengths[i + 1];

        float segmentLength = lengthAfter - lengthBefore;

        // determine where we are between the 'before' and 'after' points

        float segmentFraction = (targetArcLength - lengthBefore) / segmentLength;

        // add that fractional amount to t

        return (i + segmentFraction) / (il - 1f);

    }

    private float getUtoTmapping(float u, float distance) {
        float[] arcLengths = this.getLengths();
        int i, il = arcLengths.length;
        float targetArcLength;

        targetArcLength = distance;

        int low = 0, high = il;
        float comparison;

        while (low < high) {
            i = (int) Math.floor(low + (high - low) / 2f);
            comparison = arcLengths[i] - targetArcLength;

            if (comparison < 0f) {

                low = i + 1;

            } else if (comparison > 0f) {

                high = i - 1;

            } else {

                high = i;
                break;

                // DONE

            }

        }

        i = high;

        if (arcLengths[i] == targetArcLength) {

            return i / (il - 1f);

        }

        // we could get finer grain at lengths, or use simple interpolation between two points

        float lengthBefore = arcLengths[i];
        float lengthAfter = arcLengths[i + 1];

        float segmentLength = lengthAfter - lengthBefore;

        // determine where we are between the 'before' and 'after' points

        float segmentFraction = (targetArcLength - lengthBefore) / segmentLength;

        // add that fractional amount to t

        return (i + segmentFraction) / (il - 1);
    }
}
