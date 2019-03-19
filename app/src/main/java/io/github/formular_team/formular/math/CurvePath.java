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
import java.util.Iterator;
import java.util.List;

public class CurvePath extends Curve {
    private List<Curve> curves;

    private List<Float> cacheLengths;

    public CurvePath() {
        this.curves = new ArrayList<>();
    }

    public List<Curve> getCurves() {
        return this.curves;
    }

    public void setCurves(final List<Curve> curves) {
        this.curves = curves;
    }

    public void add(final Curve curve) {
        this.curves.add(curve);
    }

    public Curve getFirstCurve() {
        return this.curves.get(0);
    }

    public Curve getLastCurve() {
        return this.curves.get(this.curves.size() - 1);
    }

    public Curve removeLast() {
        return this.curves.remove(this.curves.size() - 1);
    }

    @Override
    public Vector2 getPointAt(final float u) {
        return this.getPoint(u);
    }

    @Override
    public Vector2 getPoint(final float t) {
        if (t == 0.0F) {
            return this.getFirstCurve().getStart();
        }
        if (t == 1.0F) {
            return this.getLastCurve().getEnd();
        }
        final float d = (this.isClosed() ? Mth.mod(t, 1.0F) : t) * this.getLength();
        final Iterator<Curve> curves = this.curves.iterator();
        final Iterator<Float> lengths = this.getCurveLengths().iterator();
        while (true) {
            final Curve curve = curves.next();
            final float length = lengths.next();
            if (curves.hasNext() && d > length) {
                continue;
            }
            final float u = Math.max(1.0F - (length - d) / curve.getLength(), 0.0F);
            return curve.getPointAt(u);
        }
    }

    // TODO remove duplicate get code
    @Override
    public float getCurvature(final float t) {
        if (t == 0.0F) {
            return this.getFirstCurve().getCurvature(0.0F);
        }
        if (t == 1.0F) {
            return this.getLastCurve().getCurvature(1.0F);
        }
        final float d = (this.isClosed() ? Mth.mod(t, 1.0F) : t) * this.getLength();
        final Iterator<Curve> curves = this.curves.iterator();
        final Iterator<Float> lengths = this.getCurveLengths().iterator();
        while (true) {
            final Curve curve = curves.next();
            final float length = lengths.next();
            if (curves.hasNext() && d > length) {
                continue;
            }
            final float u = Math.max(1.0F - (length - d) / curve.getLength(), 0.0F);
            return curve.getCurvatureAt(u);
        }
    }

    @Override
    public float getLength() {
        final List<Float> lens = this.getCurveLengths();
        return lens.get(lens.size() - 1);
    }

    public List<Float> getCurveLengths() {
        if (this.cacheLengths == null || this.cacheLengths.size() != this.curves.size()) {
            this.cacheLengths = new ArrayList<>();
            float sum = 0.0F;
            for (final Curve curve : this.curves) {
                sum += curve.getLength();
                this.cacheLengths.add(sum);
            }
        }
        return this.cacheLengths;
    }

    @Override
    public CurvePath clone() {
        return new CurvePath().copy(this);
    }

    public CurvePath copy(final CurvePath other) {
        this.curves.clear();
        for (final Curve curve : other.curves) {
            this.curves.add(curve.clone());
        }
        this.cacheLengths = null;
        return this;
    }

    @Override
    public String toString() {
        return "CurvePath{" +
            "curves=" + this.curves +
            '}';
    }
}
