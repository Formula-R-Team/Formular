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

public class CubicBezierCurve3 extends Curve {
    public Vector3 v0;

    public final Vector3 v1;

    public final Vector3 v2;

    public Vector3 v3;

    public CubicBezierCurve3(final Vector3 v0, final Vector3 v1, final Vector3 v2, final Vector3 v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    @Override
    public void setStart(final Vector2 point) {
        this.v0 = (Vector3) point;
    }

    @Override
    public void setEnd(final Vector2 point) {
        this.v3 = (Vector3) point;
    }

    @Override
    public Vector3 getPoint(final float t) {
        if (t == 0.0F) {
            return this.v0.clone();
        }
        if (t == 1.0F) {
            return this.v3.clone();
        }
        final float tx = ShapeUtils.b3(t, this.v0.getX(), this.v1.getX(), this.v2.getX(), this.v3.getX());
        final float ty = ShapeUtils.b3(t, this.v0.getY(), this.v1.getY(), this.v2.getY(), this.v3.getY());
        final float tz = ShapeUtils.b3(t, this.v0.getZ(), this.v1.getZ(), this.v2.getZ(), this.v3.getZ());
        return new Vector3(tx, ty, tz);
    }

    @Override
    public Vector2 getTangent(final float t) {
        return super.getTangent(t); // TODO
    }

    @Override
    public float getCurvature(final float t) {
        return 0.0F; // TODO
    }

    @Override
    public CubicBezierCurve3 clone() {
        return new CubicBezierCurve3(this.v0.clone(), this.v1.clone(), this.v2.clone(), this.v3.clone());
    }

    @Override
    public String toString() {
        return "CubicBezierCurve3{" +
            "v0=" + this.v0 +
            ", v1=" + this.v1 +
            ", v2=" + this.v2 +
            ", v3=" + this.v3 +
            '}';
    }
}
