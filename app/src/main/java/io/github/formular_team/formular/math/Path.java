package io.github.formular_team.formular.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Path implements Curve {
    private List<Curve> curves;

    Path(final Builder builder) {
        this.curves = builder.getCurves();
    }

    public boolean isClosed() {
        return !this.curves.isEmpty() &&
            this.curves.get(0).getPoint(0.0F).equals(
                this.curves.get(this.curves.size() - 1).getPoint(1.0F)
            );
    }

    @Override
    public boolean isStraight() {
        // TODO: curves are all colinear
        return false;
    }

    //Not sure if this is how visit should look
    public void visit(final PathVisitor visitor) {
        if (!this.curves.isEmpty()) {
            final Curve first = this.curves.get(0);
            visitor.moveTo(first instanceof LineCurve ? ((LineCurve) first).v0() : ((CubicBezierCurve) first).v0());
        }
        for (final Curve curCurve : this.curves) {
            if (curCurve instanceof LineCurve) {
                visitor.lineTo(((LineCurve) curCurve).v1());
            } else {
                final CubicBezierCurve curBezier = (CubicBezierCurve) curCurve;
                visitor.bezierCurveTo(curBezier.v1(), curBezier.v2(), curBezier.v3());
            }
        }
    }

    public Curve[] getCurves() {
        return this.curves.toArray(new Curve[0]);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Vector2 getPoint(float t) {
        if (t == 1.0F) {
            return this.curves.get(this.curves.size() - 1).getPoint(1.0F);
        }
        if ((t < 0.0F || t > 1.0F) && this.isClosed()) {
            t %= 1.0F;
            if (t < 0.0F) {
                t += 1.0F;
            }
        }
        final float d = t * this.getLength();
        final float[] curveLengths = this.getCurveLengths();
        for (int i = 0; i < curveLengths.length; i++) {
            if (curveLengths[i] >= d) {
                final float diff = curveLengths[i] - d;
                final Curve curve = this.curves.get(i);
                final float segmentLength = curve.getLength();
                final float u = segmentLength == 0 ? 0 : 1 - diff / segmentLength;
                return curve.getPointAt(u);
            }
        }
        throw new RuntimeException("t=" + t);
    }

    @Override
    public Vector2 getPointAt(final float u) {
        final float t = this.uToT(u);
        return this.getPoint(t);
    }

    @Override
    public List<Vector2> getPoints(final int divisions) {
        final List<Vector2> points = new ArrayList<>();
        Vector2 last = new Vector2(Float.NaN, Float.NaN);
        for (int i = 0; i < this.curves.size(); i++) {
            final Curve curve = this.curves.get(i);
            final int resolution = curve.isStraight() ? 1 : divisions;
            final List<Vector2> pts = curve.getPoints(resolution);
            for (final Vector2 point : pts) {
                if (!last.equals(point)) {
                    points.add(point);
                    last = point;
                }
            }
        }
        return points;
    }

    public List<Vector2> getPoints() {
        return this.getPoints(12);
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
        return this.getSpacedPoints(40);
    }

    @Override
    public float getLength() {
        final float[] lens = this.getCurveLengths();
        return lens[lens.length - 1];
    }

    @Override
    public float[] getLengths(final int divisions) {
        final float[] lengths = new float[1 + divisions];
        Vector2 last = this.getPoint(0.0F);
        float sum = 0.0F;
        for (int p = 1; p <= divisions; p++) {
            final Vector2 current = this.getPoint((float) p / divisions);
            sum += current.distanceTo(last);
            lengths[p] = sum;
            last = current;
        }
        return lengths;
    }

    public float[] getLengths() {
        return this.getLengths(200);
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

    public float[] getCurveLengths() {
        final float[] lengths = new float[this.curves.size()];
        float sums = 0.0F;
        for (int i = 0; i < this.curves.size(); i++) {
            sums += this.curves.get(i).getLength();
            lengths[i] = sums;
        }
        return lengths;
    }

    public void copy(final Path path) {
        this.curves = Arrays.stream(path.getCurves()).map(Curve::copy).collect(Collectors.toList());
    }

    public Curve copy() {
        final Builder newBuilder = new Builder();
        this.copy(newBuilder);
        return newBuilder.build();
    }

    protected void copy(final Builder newBuilder) {
        for (final Curve curCurve : this.curves) {
            if (curCurve instanceof LineCurve) {
                newBuilder.lineTo(((LineCurve) curCurve).v1());
            } else {
                final CubicBezierCurve curBezier = (CubicBezierCurve) curCurve;
                newBuilder.bezierCurveTo(curBezier.v1(), curBezier.v2(), curBezier.v3());
            }
        }
    }

    // TODO: remove duplicate code
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

    public static class Builder implements PathVisitor {
        private Vector2 currentPoint;

        private ArrayList<Curve> curves;

        Builder() {
            this.currentPoint = new Vector2();
            this.curves = new ArrayList<>();
        }

        public Builder(final Vector2[] points) {
            this.setFromPoints(points);
        }

        public boolean isEmpty() {
            return this.curves.isEmpty();
        }

        private Builder setFromPoints(final Vector2[] points) {
            this.moveTo(points[0]);
            for (int i = 1; i < points.length; i++) {
                this.lineTo(points[i]);
            }
            return this;
        }

        public ArrayList<Curve> getCurves() {
            return this.curves;
        }

        @Override
        public Builder moveTo(final Vector2 point) {

            this.currentPoint = point.copy();
            return this;
        }


        @Override
        public Builder lineTo(final Vector2 point) {
            final LineCurve curve = new LineCurve(this.currentPoint.copy(), point.copy());
            this.curves.add(curve);
            this.currentPoint = point.copy();
            return this;
        }

        @Override
        public Builder bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
            final CubicBezierCurve curve = new CubicBezierCurve(this.currentPoint.copy(), controlA.copy(), controlB.copy(), point.copy());
            this.curves.add(curve);
            this.currentPoint = point.copy();
            return this;
        }

        @Override
        public Builder closePath() {
            return this.lineTo(this.curves.get(0).getPoint(0).copy());
        }

        public Path build() {
            return new Path(this);
        }
    }
}
