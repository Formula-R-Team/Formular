/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 *
 * This file is part of Parallax project.
 *
 * Parallax is free software: you can redistribute it and/or modify it
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 *
 * Parallax is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution
 * 3.0 Unported License. for more details.
 *
 * You should have received a copy of the the Creative Commons Attribution
 * 3.0 Unported License along with Parallax.
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package io.github.formular_team.formular.core.math;

import java.util.ArrayList;
import java.util.List;

public abstract class Curve {
    public List<Float> cacheArcLengths;

    public abstract void setStart(final Vector2 point);

    public Vector2 getStart() {
        return this.getPoint(0.0F);
    }

    public abstract void setEnd(final Vector2 point);

    public Vector2 getEnd() {
        return this.getPoint(1.0F);
    }

    public boolean isClosed() {
        return this.getStart().equals(this.getEnd());
    }

    public abstract Vector2 getPoint(float t);

    public Vector2 getPointAt(final float u) {
        final float t = this.getUtoTmapping(u);
        return this.getPoint(t);
    }

    public List<Vector2> getPoints() {
        return this.getPoints(5);
    }

    public List<Vector2> getPoints(final int divisions) {
        final List<Vector2> pts = new ArrayList<>();
        for (int d = 0; d <= divisions; d++) {
            pts.add(this.getPoint(d / (float) divisions));
        }
        return pts;
    }

    public List<Vector2> getSpacedPoints() {
        return this.getSpacedPoints(5);
    }

    public List<Vector2> getSpacedPoints(final int divisions) {
        final List<Vector2> pts = new ArrayList<>();
        for (int d = 0; d <= divisions; d++) {
            pts.add(this.getPointAt(d / (float) divisions));
        }
        return pts;
    }

    public float getLength() {
        final List<Float> lengths = this.getLengths();
        return lengths.get(lengths.size() - 1);
    }

    public List<Float> getLengths() {
        return this.getLengths(200);
    }

    public List<Float> getLengths(final int divisions) {
        if (this.cacheArcLengths == null || this.cacheArcLengths.size() != (divisions + 1)) {
            this.cacheArcLengths = new ArrayList<>();
            this.cacheArcLengths.add(0.0F);
            Vector2 last = this.getPoint(0.0F);
            float sum = 0.0F;
            for (int p = 1; p <= divisions; p++) {
                final Vector2 current = this.getPoint(p / (float) divisions);
                sum += current.distanceTo(last);
                this.cacheArcLengths.add(sum);
                last = current;
            }
        }
        return this.cacheArcLengths;
    }

    public Box2 getBounds(final int divisions) {
        return new Box2().setFromPoints(this.getSpacedPoints(divisions));
    }

    public void refresh() {
        this.cacheArcLengths = null;
        this.getLengths();
    }

    public float getUtoTmapping(final float u) {
        final List<Float> arcLengths = this.getLengths();
        final int end = arcLengths.size() - 1;
        final float distance = u * arcLengths.get(end);
        int high = end;
        for (int low = 0; low <= high; ) {
            final int i = low + (high - low) / 2;
            final float comparison = arcLengths.get(i) - distance;
            if (comparison < 0) {
                low = i + 1;
            } else if (comparison > 0) {
                high = i - 1;
            } else {
                high = i;
                break;
            }
        }
        if (arcLengths.get(high) == distance) {
            return high / (float) end;
        }
        final float lengthBefore = arcLengths.get(high);
        final float lengthAfter = arcLengths.get(high + 1);
        final float segmentLength = lengthAfter - lengthBefore;
        final float segmentFraction = (distance - lengthBefore) / segmentLength;
        return (high + segmentFraction) / (float) end;
    }

    public Vector2 getTangent(final float t) {
        final float delta = 0.0001F;
        float t1 = t - delta;
        float t2 = t + delta;
        if (!this.isClosed()) {
            t1 = Math.max(0.0F, t1);
            t2 = Math.min(1.0F, t2);
        }
        return this.getPoint(t2).sub(this.getPoint(t1)).normalize();
    }

    public Vector2 getTangentAt(final float u) {
        return this.getTangent(this.getUtoTmapping(u));
    }

    public abstract float getCurvature(final float t);

    public float getCurvatureAt(final float u) {
        return this.getCurvature(this.getUtoTmapping(u));
    }

    @Override
    public abstract Curve clone();
}