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
    public final Vector2 v0;

    public final Vector2 v1;

    public final Vector2 v2;

    public final Vector2 v3;

    public CubicBezierCurve(final Vector2 v0, final Vector2 v1, final Vector2 v2, final Vector2 v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
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
        final float tx = CurveUtils.tangentCubicBezier(t, this.v0.getX(), this.v1.getX(), this.v2.getX(), this.v3.getX());
        final float ty = CurveUtils.tangentCubicBezier(t, this.v0.getY(), this.v1.getY(), this.v2.getY(), this.v3.getY());
        final Vector2 tangent = new Vector2(tx, ty);
        tangent.normalize();
        return tangent;
    }

    @Override
    public CubicBezierCurve clone() {
        return new CubicBezierCurve(this.v0.clone(), this.v1.clone(), this.v2.clone(), this.v3.clone());
    }
}
