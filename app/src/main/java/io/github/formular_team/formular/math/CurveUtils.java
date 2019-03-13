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

/**
 * This class implements some helpers methods for Curve instances
 * <p>
 * This code is based on js-code written by zz85
 * http://www.lab4games.net/zz85/blog
 *
 * @author thothbot
 */
public final class CurveUtils {
    private CurveUtils() {}

    /**
     * This method calculates tangent of quadratic Bezier curve.
     *
     * @param t  the value in range <0.0, 1.0>. The t in the
     *           function for a linear Bezier curve can be
     *           thought of as describing how far B(t) is from p0 to p2.
     * @param p0 the p0 Quadratic Bezier Curve point.
     * @param p1 the p1 Quadratic Bezier Curve point.
     * @param p2 the p2 Quadratic Bezier Curve point.
     * @return the tangent of Quadratic Bezier Curve
     */
    public static float tangentQuadraticBezier(final float t, final float p0, final float p1, final float p2) {
        return 2.0F * (1.0F - t) * (p1 - p0) + 2.0F * t * (p2 - p1);
    }

    /**
     * Compute tangent of cubic Bezier curve.
     *
     * @param t  the value in range <0.0, 1.0>. The t in the
     *           function for a linear Bezier curve can be
     *           thought of as describing how far B(t) is from p0 to p3.
     * @param p0 the p0 Cubic Bezier Curve point.
     * @param p1 the p1 Cubic Bezier Curve point.
     * @param p2 the p2 Cubic Bezier Curve point.
     * @param p3 the p3 Cubic Bezier Curve point.
     * @return the tangent of Cubic Bezier Curve
     */
    public static float derivativeCubicBezier(final float t, final float p0, final float p1, final float p2, final float p3) {
        return 3.0F * p1 * (1.0F - t) * (1.0F - t) -
            3.0F * p0 * (1.0F - t) * (1.0F - t) +
            6.0F * t * p2 * (1.0F - t) -
            6.0F * t * p1 * (1.0F - t) +
            3.0F * t * t * p3 -
            3.0F * t * t * p2;
    }

    public static float secondDerivativeCubicBezier(final float t, final float p0, final float p1, final float p2, final float p3) {
        return 6.0F * (1.0F - t) * (p2 - 2.0F * p1 + p0) + 6.0F * t * (p3 - 2.0F * p2 + p1);
    }

    /**
     * Interpolation of Catmull-Rom spline
     *
     * @param p0 the p0 Spline point.
     * @param p1 the p1 Spline point.
     * @param p2 the p2 Spline point.
     * @param p3 the p3 Spline point.
     * @param t  the value in range <0.0, 1.0>. The t in the
     *           function for a linear Bezier curve can be
     *           thought of as describing how far B(t) is from p0 to p3.
     * @return the interpolated value.
     */
    public static float interpolate(final float p0, final float p1, final float p2, final float p3, final float t) {
        final float v0 = (p2 - p0) * 0.5F;
        final float v1 = (p3 - p1) * 0.5F;
        final float t2 = t * t;
        final float t3 = t * t2;
        return (2.0F * p1 - 2.0F * p2 + v0 + v1) * t3 +
            (-3.0F * p1 + 3.0F * p2 - 2.0F * v0 - v1) * t2 +
            v0 * t + p1;
    }
}