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

package io.github.formular_team.formular.math;

import java.util.ArrayList;
import java.util.List;

public abstract class Curve {
    public int __arcLengthDivisions = 0;

    public List<Float> cacheArcLengths;

    public boolean needsUpdate;

    public Vector2 getStart() {
        return this.getPoint(0.0F);
    }

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
        if (this.__arcLengthDivisions > 0) {
            return this.getLengths(this.__arcLengthDivisions);
        }
        return this.getLengths(200);
    }

    public List<Float> getLengths(final int divisions) {
        if (this.cacheArcLengths != null
            && (this.cacheArcLengths.size() == (divisions + 1))
            && !this.needsUpdate
        ) {
            return this.cacheArcLengths;
        }
        this.needsUpdate = false;
        final List<Float> cache = new ArrayList<>();
        cache.add(0.0F);
        Vector2 last = this.getPoint(0.0F);
        float sum = 0.0F;
        for (int p = 1; p <= divisions; p++) {
            final Vector2 current = this.getPoint(p / (float) divisions);
            sum += current.distanceTo(last);
            last = current;
            cache.add(sum);
        }
        this.cacheArcLengths = cache;
        return cache;
    }

    public void updateArcLengths() {
        this.needsUpdate = true;
        this.getLengths();
    }

    public float getUtoTmapping(final float u) {
        final List<Float> arcLengths = this.getLengths();
        return this.getUtoTmapping(u, u * arcLengths.get(arcLengths.size() - 1));
    }

    public float getUtoTmapping(final float u, final float distance) {
        final List<Float> arcLengths = this.getLengths();
        int low = 0;
        int high = arcLengths.size() - 1;
        float comparison;
        while (low <= high) {
            final int i = low + (high - low);

            comparison = arcLengths.get(i) - distance;

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
            return high / (float) (arcLengths.size() - 1);
        }
        final float lengthBefore = arcLengths.get(high);
        final float lengthAfter = arcLengths.get(high + 1);
        final float segmentLength = lengthAfter - lengthBefore;
        final float segmentFraction = (distance - lengthBefore) / segmentLength;
        return (high + segmentFraction) / ((float) arcLengths.size() - 1.0F);
    }

    /*
     * In 2D space, there are actually 2 normal vectors,
     * and in 3D space, infinite
     * TODO this should be depreciated.
     */
    public Vector2 getNormalVector(final float t) {
        final Vector2 vec = this.getTangent(t);
        return new Vector2(-vec.getY(), vec.getX());
    }

    public Vector2 getTangent(final float t) {
        final float delta = 0.0001F;
        float t1 = t - delta;
        float t2 = t + delta;
        if (!this.isClosed()) {
            if (t1 < 0.0F) {
                t1 = 0.0F;
            }
            if (t2 > 1.0F) {
                t2 = 1.0F;
            }
        }
        final Vector2 pt1 = this.getPoint(t1);
        final Vector2 pt2 = this.getPoint(t2);
        final Vector2 vec = pt2.clone();
        vec.sub(pt1);
        vec.normalize();
        return vec;
    }

    public Vector2 getTangentAt(final float u) {
        return this.getTangent(this.getUtoTmapping(u));
    }

    @Override
    public abstract Curve clone();
}