package io.github.formular_team.formular.math;

import com.google.common.collect.Lists;

import java.util.List;

import static io.github.formular_team.formular.math.Interpolations.CubicBezier;

public final class CubicBezierCurve implements Curve {
    private final Vector2 v0;

    private final Vector2 v1;

    private final Vector2 v2;

    private final Vector2 v3;

    public CubicBezierCurve() {
        this(new Vector2(), new Vector2(), new Vector2(), new Vector2());
    }

    public CubicBezierCurve(final Vector2 v0, final Vector2 v1, final Vector2 v2, final Vector2 v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;

    }

    public Vector2 v0() {
        return this.v0;
    }

    public Vector2 v1() {
        return this.v1;
    }

    public Vector2 v2() {
        return this.v2;
    }

    public Vector2 v3() {
        return this.v3;
    }

    @Override
    public boolean isStraight() {
        // TODO: return true when vertices are colinear
        return false;
    }

    @Override
    public Vector2 getPoint(final float t) {
        final Vector2 point = this.v0.copy();
        if (t == 1.0F) {
            point.copy(this.v3);
        } else if (t != 0.0F) {
            point.set(
                CubicBezier(t, this.v0.x(), this.v1.x(), this.v2.x(), this.v3.x()),
                CubicBezier(t, this.v0.y(), this.v1.y(), this.v2.y(), this.v3.y())
            );
        }
        return point;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        final float t = this.uToT(u);
        return this.getPoint(t);
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
            points[d] = this.getPoint((float) d / divisions);
        }
        return points;
    }

    @Override
    public Vector2[] getSpacedPoints() {
        return this.getSpacedPoints(5);
    }

    @Override
    public float getLength() {
        final float[] lengths = this.getLengths();
        return lengths[lengths.length - 1];
    }

    @Override
    public float[] getLengths(final int divisions) {
        final float[] lengths = new float[1 + divisions];
        Vector2 current, last = this.getPoint(0.0F);
        float sum = 0;
        for (int p = 1; p <= divisions; p++) {
            current = this.getPoint((float) p / divisions);
            sum += current.distanceTo(last);
            lengths[p] = sum;
            last = current;
        }
        return lengths;
    }

    @Override
    public Vector2 getTangent(final float t) {
        final float delta = 0.0001F;
        float t1 = t - delta;
        float t2 = t + delta;
        if (t1 < 0.0F) {
            t1 = 0.0F;
        }
        if (t2 > 1.0F) {
            t2 = 1.0F;
        }
        final Vector2 pt1 = this.getPoint(t1);
        final Vector2 pt2 = this.getPoint(t2);
        final Vector2 vec = pt2.copy();
        vec.sub(pt1);
        vec.normalize();
        return vec;
    }

    @Override
    public Vector2 getTangentAt(final float u) {
        final float t = this.uToT(u);
        return this.getTangent(t);
    }

    @Override
    public Curve copy() {
        final CubicBezierCurve toReturn = new CubicBezierCurve();
        toReturn.v0().copy(this.v0);
        toReturn.v1().copy(this.v1);
        toReturn.v2().copy(this.v2);
        toReturn.v3().copy(this.v3);
        return toReturn;
    }

    public void copy(final CubicBezierCurve in) {
        this.v0.copy(in.v0());
        this.v1.copy(in.v1());
        this.v2.copy(in.v2());
        this.v3.copy(in.v3());
    }

    private float uToT(final float u) {
        final float[] lengths = this.getLengths();
        return this.uToT(lengths, u * lengths[lengths.length - 1]);
    }

    private float uToT(final float[] arcLengths, final float distance) {
        int i, low = 0, high = arcLengths.length;
        while (low < high) {
            i = low + (high - low) / 2;
            final float delta = arcLengths[i] - distance;
            if (delta < 0.0F) {
                low = i + 1;
            } else if (delta > 0.0F) {
                high = i - 1;
            } else {
                high = i;
                break;
            }
        }
        i = high;
        if (arcLengths[i] == distance) {
            return i / (arcLengths.length - 1f);
        }
        final float lengthBefore = arcLengths[i];
        final float lengthAfter = arcLengths[i + 1];
        final float segmentLength = lengthAfter - lengthBefore;
        final float segmentFraction = (distance - lengthBefore) / segmentLength;
        return (i + segmentFraction) / (arcLengths.length - 1);
    }
}
