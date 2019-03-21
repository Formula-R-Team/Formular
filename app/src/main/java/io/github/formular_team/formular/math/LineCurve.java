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

public class LineCurve extends Curve {
    private Vector2 v1;

    private Vector2 v2;

    public LineCurve(final Vector2 v1, final Vector2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public void setStart(final Vector2 point) {
        this.v1 = point;
    }

    @Override
    public void setEnd(final Vector2 point) {
        this.v2 = point;
    }

    @Override
    public Vector2 getPoint(final float t) {
        if (t == 0.0F) {
            return this.v1.clone();
        }
        if (t == 1.0F) {
            return this.v2.clone();
        }
        final Vector2 point = this.v2.clone();
        point.sub(this.v1);
        point.multiply(t);
        point.add(this.v1);
        return point;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        return this.getPoint(u);
    }

    @Override
    public Vector2 getTangent(final float t) {
        return this.v2.clone().sub(this.v1).normalize();
    }

    @Override
    public float getCurvature(final float t) {
        return 0.0F;
    }

    @Override
    public float getLength() {
        return this.v1.distanceTo(this.v2);
    }

    @Override
    public List<Float> getLengths(final int divisions) {
        final List<Float> lengths = new ArrayList<>(1 + divisions);
        final float length = this.getLength();
        for (int n = 0; n <= divisions; n++) {
            lengths.add(length * n / (float) divisions);
        }
        return lengths;
    }

    @Override
    public LineCurve clone() {
        return new LineCurve(this.v1.clone(), this.v2.clone());
    }

    @Override
    public String toString() {
        return "LineCurve{" +
            "v1=" + this.v1 +
            ", v2=" + this.v2 +
            '}';
    }
}