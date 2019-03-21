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

public class CubicBezierCurve extends Curve {
    public Vector2 v0;

    public final Vector2 v1;

    public final Vector2 v2;

    public Vector2 v3;

    public CubicBezierCurve(final Vector2 v0, final Vector2 v1, final Vector2 v2, final Vector2 v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    @Override
    public void setStart(final Vector2 point) {
        this.v0 = point;
    }

    @Override
    public void setEnd(final Vector2 point) {
        this.v3 = point;
    }

    public CubicBezierCurve reorient() {
        return new CubicBezierCurve(this.v3, this.v2, this.v1, this.v0);
    }

    @Override
    public Vector2 getPoint(final float t) {
        if (t == 0.0F) {
            return this.v0.clone();
        }
        if (t == 1.0F) {
            return this.v3.clone();
        }
        final float tx = ShapeUtils.b3(t, this.v0.getX(), this.v1.getX(), this.v2.getX(), this.v3.getX());
        final float ty = ShapeUtils.b3(t, this.v0.getY(), this.v1.getY(), this.v2.getY(), this.v3.getY());
        return new Vector2(tx, ty);
    }

    @Override
    public Vector2 getTangent(final float t) {
        final float tx = CurveUtils.derivativeCubicBezier(t, this.v0.getX(), this.v1.getX(), this.v2.getX(), this.v3.getX());
        final float ty = CurveUtils.derivativeCubicBezier(t, this.v0.getY(), this.v1.getY(), this.v2.getY(), this.v3.getY());
        return new Vector2(tx, ty).normalize();
    }

    @Override
    public float getCurvature(final float t) {
        if (t == 0.0F) {
            return this.getCurvature();
        }
        if (t == 1.0F) {
            return -this.reorient().getCurvature();
        }
        final CubicBezierCurve[] subCurves = this.split(t);
        if (t <= 0.5F) {
            return subCurves[1].getCurvature(0.0F);
        }
        return subCurves[0].getCurvature(1.0F);
    }

    private float getCurvature() {
        final float a = this.v1.distanceTo(this.v0);
        final float b = this.v1.clone().sub(this.v0).cross(this.v2.clone().sub(this.v1));
        return 2.0F / 3.0F * b / (a * a * a);
    }

    public CubicBezierCurve[] split(final float t) {
        final Vector2 v01 = this.v0.clone().lerp(this.v1, t);
        final Vector2 v12 = this.v1.clone().lerp(this.v2, t);
        final Vector2 v23 = this.v2.clone().lerp(this.v3, t);
        final Vector2 vv1 = v01.clone().lerp(v12, t);
        final Vector2 vv2 = v12.clone().lerp(v23, t);
        final Vector2 vvv = vv1.clone().lerp(vv2, t);
        return new CubicBezierCurve[] {
            new CubicBezierCurve(this.v0, v01, vv1, vvv),
            new CubicBezierCurve(vvv, vv2, v23, this.v3)
        };
    }

    @Override
    public CubicBezierCurve clone() {
        return new CubicBezierCurve(this.v0.clone(), this.v1.clone(), this.v2.clone(), this.v3.clone());
    }

    @Override
    public String toString() {
        return "CubicBezierCurve{" +
            "v0=" + this.v0 +
            ", v1=" + this.v1 +
            ", v2=" + this.v2 +
            ", v3=" + this.v3 +
            '}';
    }
}
