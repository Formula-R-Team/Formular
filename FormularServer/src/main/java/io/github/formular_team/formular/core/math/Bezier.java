package io.github.formular_team.formular.core.math;

import java.util.List;

/**
 * An Algorithm for Automatically Fitting Digitized Curves
 * by Philip J. Schneider
 * from "Graphics Gems", Academic Press, 1990
 * http://www.realtimerendering.com/resources/GraphicsGems/gems/FitCurves.c
 */
public final class Bezier {
    private Bezier() {}

    /*
     * Fit a Bezier curve to a set of digitized points.
     */
    public static Path fitCurve(final Path path, final float error) {
        final boolean closed = path.isClosed();
        final List<Vector2> d = path.getPoints(closed);
        final Path bezierPath = new Path();
        bezierPath.moveTo(d.get(0).getX(), d.get(0).getY());
        final Vector2 tHat1, tHat2;
        if (closed) {
            tHat2 = computeCenterTangent(d, 0);
            tHat1 = tHat2.clone().negate();
        } else {
            tHat1 = computeLeftTangent(d, 0);
            tHat2 = computeRightTangent(d, d.size() - 1);
        }
        fitCubic(d, 0, d.size() - 1, tHat1, tHat2, error, bezierPath);
        return bezierPath;
    }

    /*
     * Fit a Bezier curve to a (sub)set of digitized points.
     */
    private static void fitCubic(final List<Vector2> d, final int first, final int last, final Vector2 tHat1, final Vector2 tHat2, final float error, final PathVisitor bezierPath) {
        final float iterationError = error * error;
        final int nPts = last - first + 1;
        // Use heuristic if region only has two points in it
        // Control points of fitted Bezier curve
        if (nPts == 2) {
            final float dist = d.get(last).distanceTo(d.get(first)) / 3.0F;
            final Vector2[] bezCurve = new Vector2[4];
            for (int i = 0; i < bezCurve.length; i++) {
                bezCurve[i] = new Vector2();
            }
            bezCurve[0] = d.get(first);
            bezCurve[3] = d.get(last);
            bezCurve[1].add(bezCurve[0], tHat1.setLength(dist));
            bezCurve[2].add(bezCurve[3], tHat2.setLength(dist));
            bezierPath.bezierCurveTo(
                bezCurve[1].x, bezCurve[1].y,
                bezCurve[2].x, bezCurve[2].y,
                bezCurve[3].x, bezCurve[3].y
            );
            return;
        }
        // Parameterize points, and attempt to fit curve
        float[] u = chordLengthParameterize(d, first, last);
        Vector2[] bezCurve = generateBezier(d, first, last, u, tHat1, tHat2);
        // Find max deviation of points to fitted curve maximum fitting error point to split point set at.
        final int[] splitPoint = new int[1];
        float maxError = computeMaxError(d, first, last, bezCurve, u, splitPoint);
        if (maxError < error) {
            bezierPath.bezierCurveTo(
                bezCurve[1].x, bezCurve[1].y,
                bezCurve[2].x, bezCurve[2].y,
                bezCurve[3].x, bezCurve[3].y
            );
            return;
        }
        // If error not too large, try some reparameterization and iteration
        if (maxError < iterationError) {
            for (int i = 0; i < 4; i++) {
                final float[] uPrime = reparameterize(d, first, last, u, bezCurve);
                bezCurve = generateBezier(d, first, last, uPrime, tHat1, tHat2);
                maxError = computeMaxError(d, first, last, bezCurve, uPrime, splitPoint);
                if (maxError < error) {
                    bezierPath.bezierCurveTo(
                        bezCurve[1].x, bezCurve[1].y,
                        bezCurve[2].x, bezCurve[2].y,
                        bezCurve[3].x, bezCurve[3].y
                    );
                    return;
                }
                u = uPrime;
            }
        }
        // Fitting failed -- split at max error point and fit recursively
        // Unit tangent vector at splitPoint
        final Vector2 tHatCenter = computeCenterTangent(d, splitPoint[0]);
        fitCubic(d, first, splitPoint[0], tHat1, tHatCenter, error, bezierPath);
        tHatCenter.negate();
        fitCubic(d, splitPoint[0], last, tHatCenter, tHat2, error, bezierPath);
    }

    /*
     * Use least-squares method to find Bezier control points for region.
     */
    private static Vector2[] generateBezier(final List<Vector2> d, final int first, final int last, final float[] uPrime, final Vector2 tHat1, final Vector2 tHat2) {
        // Number of pts in sub-curve
        final int nPts = last - first + 1;
        // Precomputed rhs for eqn
        final Vector2[][] A = new Vector2[nPts][2];
        // Compute the A's
        for (int i = 0; i < nPts; i++) {
            A[i][0] = tHat1.clone().setLength(b1(uPrime[i]));
            A[i][1] = tHat2.clone().setLength(b2(uPrime[i]));
        }
        // Create the C and X matrices
        final float[][] C = new float[2][2];
        C[0][0] = 0.0F;
        C[0][1] = 0.0F;
        C[1][0] = 0.0F;
        C[1][1] = 0.0F;
        final float[] X = new float[2];
        X[0] = 0.0F;
        X[1] = 0.0F;
        for (int i = 0; i < nPts; i++) {
            C[0][0] += A[i][0].dot(A[i][0]);
            C[0][1] += A[i][0].dot(A[i][1]);
            // C[1][0] += A[i][0].dot(A[i][1]);
            C[1][0] = C[0][1];
            C[1][1] += A[i][1].dot(A[i][1]);
            final Vector2 tmp = d.get(first + i).clone()
                .sub(d.get(first).clone()
                .multiply(b0(uPrime[i])).clone()
                .add(d.get(first).clone()
                .multiply(b1(uPrime[i])).clone()
                .add(d.get(last).clone()
                .multiply(b2(uPrime[i])).clone()
                .add(d.get(last).clone()
                .multiply(b3(uPrime[i]))))));
            X[0] += A[i][0].dot(tmp);
            X[1] += A[i][1].dot(tmp);
        }
        // Compute the determinants of C and X
        float det_C0_C1 = C[0][0] * C[1][1] - C[1][0] * C[0][1];
        final float det_C0_X = C[0][0] * X[1] - C[0][1] * X[0];
        final float det_X_C1 = X[0] * C[1][1] - X[1] * C[0][1];
        // Finally, derive alpha values
        if (det_C0_C1 == 0.0F) {
            det_C0_C1 = C[0][0] * C[1][1] * 10.0e-12F;
        }
        /* Alpha values, left and right	*/
        final float alpha_l = det_X_C1 / det_C0_C1;
        final float alpha_r = det_C0_X / det_C0_C1;
        /*
         * If alpha negative, use the Wu/Barsky heuristic (see text)
         * (if alpha is 0, you get coincident control points that lead to
         * divide by zero in any subsequent NewtonRaphsonRootFind() call.
         */
        if (alpha_l < 1.0e-6F || alpha_r < 1.0e-6F) {
            // fall back on standard (probably inaccurate) formula, and subdivide further if needed.
            final float dist = d.get(last).distanceTo(d.get(first)) / 3.0F;
            final Vector2[] bezCurve = new Vector2[] {
                d.get(first),
                d.get(first),
                d.get(last),
                d.get(last)
            };
            if (dist != 0.0F) {
                bezCurve[1] = bezCurve[1].clone().add(tHat1.setLength(dist));
                bezCurve[2] = bezCurve[2].clone().add(tHat2.setLength(dist));
            }
            return bezCurve;
        }
        /*
         * First and last control points of the Bezier curve are
         * positioned exactly at the first and last data points
         * Control points 1 and 2 are positioned an alpha distance out
         * on the tangent vectors, left and right, respectively
         */
        return new Vector2[] {
            d.get(first),
            d.get(first).clone().add(tHat1.setLength(alpha_l)),
            d.get(last).clone().add(tHat2.setLength(alpha_r)),
            d.get(last)
        };
    }

    /*
     * Given set of points and their parameterization, try to find
     * a better parameterization.
     */
    private static float[] reparameterize(final List<Vector2> d, final int first, final int last, final float[] u, final Vector2[] bezCurve) {
        final float[] uPrime = new float[last - first + 1];
        for (int i = first; i <= last; i++) {
            uPrime[i - first] = newtonRaphsonRootFind(bezCurve, d.get(i), u[i - first]);
        }
        return uPrime;
    }

    /*
     * Use Newton-Raphson iteration to find better root.
     */
    private static float newtonRaphsonRootFind(final Vector2[] Q, final Vector2 P, final float u) {
        // Q' and Q''
        final Vector2[] Q1 = new Vector2[3];
        final Vector2[] Q2 = new Vector2[2];
        // Compute Q(u)
        final Vector2 Q_u = bezierII(3, Q, u);
        // Generate control vertices for Q'
        for (int i = 0; i <= 2; i++) {
            Q1[i] = new Vector2(
                (Q[i + 1].x - Q[i].x) * 3.0F,
                (Q[i + 1].y - Q[i].y) * 3.0F
            );
        }
        // Generate control vertices for Q''
        for (int i = 0; i <= 1; i++) {
            Q2[i] = new Vector2(
                (Q1[i + 1].x - Q1[i].x) * 2.0F,
                (Q1[i + 1].y - Q1[i].y) * 2.0F
            );
        }
        // Compute Q'(u) and Q''(u)
        final Vector2 Q1_u = bezierII(2, Q1, u);
        final Vector2 Q2_u = bezierII(1, Q2, u);
        // Compute f(u)/f'(u)
        final float numerator = (Q_u.x - P.x) * Q1_u.x + (Q_u.y - P.y) * Q1_u.y;
        final float denominator = Q1_u.x * Q1_u.x + Q1_u.y * Q1_u.y + (Q_u.x - P.x) * Q2_u.x + (Q_u.y - P.y) * Q2_u.y;
        // u = u - f(u)/f'(u)
        return u - numerator / denominator;
    }

    /*
     * Evaluate a Bezier curve at a particular parameter value.
     */
    private static Vector2 bezierII(final int degree, final Vector2[] V, final float t) {
        final Vector2[] Vtemp = new Vector2[degree + 1];
        for (int i = 0; i <= degree; i++) {
            Vtemp[i] = V[i].clone();
        }
        for (int i = 1; i <= degree; i++) {
            for (int j = 0; j <= degree - i; j++) {
                Vtemp[j].x = (1.0F - t) * Vtemp[j].x + t * Vtemp[j + 1].x;
                Vtemp[j].y = (1.0F - t) * Vtemp[j].y + t * Vtemp[j + 1].y;
            }
        }
        return Vtemp[0];
    }

    /*
     * B0, B1, B2, B3 :
     * Bezier multipliers
     */
    private static float b0(final float u) {
        final float tmp = 1.0F - u;
        return tmp * tmp * tmp;
    }

    private static float b1(final float u) {
        final float tmp = 1.0F - u;
        return 3.0F * u * (tmp * tmp);
    }

    private static float b2(final float u) {
        final float tmp = 1.0F - u;
        return 3.0F * u * u * tmp;
    }

    private static float b3(final float u) {
        return u * u * u;
    }

    /*
     * Approximate unit tangents at endpoints and "center" of digitized curve.
     */
    private static Vector2 computeLeftTangent(final List<Vector2> d, final int end) {
        return d.get(end + 1).clone().sub(d.get(end));
    }

    private static Vector2 computeRightTangent(final List<Vector2> d, final int end) {
        return d.get(end - 1).clone().sub(d.get(end));
    }

    private static Vector2 computeCenterTangent(final List<Vector2> d, final int center) {
        final Vector2 V1 = d.get((center + d.size() - 1) % d.size()).clone().sub(d.get(center));
        final Vector2 V2 = d.get(center).clone().sub(d.get((center + 1) % d.size()));
        return V1.add(V2).divide(2.0F).normalize();
    }

    /*
     * Assign parameter values to digitized points using relative distances between points.
     */
    private static float[] chordLengthParameterize(final List<Vector2> d, final int first, final int last) {
        final float[] u = new float[last - first + 1];
        u[0] = 0.0F;
        for (int i = first + 1; i <= last; i++) {
            u[i - first] = u[i - first - 1] + d.get(i).distanceTo(d.get(i - 1));
        }
        for (int i = first + 1; i <= last; i++) {
            u[i - first] = u[i - first] / u[last - first];
        }
        return u;
    }

    /*
     * Find the maximum squared distance of digitized points to fitted curve.
     */
    private static float computeMaxError(final List<Vector2> d, final int first, final int last, final Vector2[] bezCurve, final float[] u, final int[] splitPoint) {
        splitPoint[0] = (last - first + 1) / 2;
        if (bezCurve[0].equals(bezCurve[1]) || bezCurve[3].equals(bezCurve[2])) {
            return Float.POSITIVE_INFINITY;
        }
        float maxDist = 0.0F;
        for (int i = first + 1; i < last; i++) {
            final Vector2 P = bezierII(3, bezCurve, u[i - first]);
            final Vector2 v = P.clone().sub(d.get(i));
            final float dist = v.lengthSq();
            if (dist >= maxDist) {
                maxDist = dist;
                splitPoint[0] = i;
            }
        }
        return maxDist;
    }
}
