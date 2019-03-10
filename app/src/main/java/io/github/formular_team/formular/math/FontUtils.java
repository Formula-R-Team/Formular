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
import java.util.Arrays;
import java.util.List;

public final class FontUtils {
    public static final float EPSILON = 0.00000001F;

    private FontUtils() {}

    /*
     * @param contour
     * @param result
     * There will be generated List of List of {@link Vector2}
     * @param vertIndices
     * There will be generated List of List of integer
     */
    public static void triangulate(final List<Vector2> contour, final List<List<Vector2>> result, final List<List<Integer>> vertIndices) {
        final int n = contour.size();

        if (n < 3) {
            return;
        }

        final List<Integer> verts = new ArrayList<>();

        if (area(contour) > 0.0F) {
            for (int v = 0; v < n; v++) {
                verts.add(v);
            }
        } else {
            for (int v = 0; v < n; v++) {
                verts.add((n - 1) - v);
            }
        }

        int nv = n;
        /*  remove nv-2 Vertices, creating 1 triangle every time */
        int count = 2 * nv;   /* error detection */

        for (int v = nv - 1; nv > 2; ) {
            /* if we loop, it is probably a non-simple polygon */

            if (0 >= (count--)) {
                //** Triangulate: ERROR - probable bad polygon!

                //throw ( "Warning, unable to triangulate polygon!" );
                //return null;
                // Sometimes warning is fine, especially polygons are triangulated in reverse.
//                Log.w(TAG, "Unable to triangulate polygon!");

                return;
            }

            /* three consecutive vertices in current polygon, <u,v,w> */

            int u = v;
            if (nv <= u) {
                u = 0;     /* previous */
            }
            v = u + 1;
            if (nv <= v) {
                v = 0;     /* new v    */
            }
            int w = v + 1;
            if (nv <= w) {
                w = 0;     /* next     */
            }

            if (snip(contour, u, v, w, nv, verts)) {
                /* true names of the vertices */
                final int a = verts.get(u);
                final int b = verts.get(v);
                final int c = verts.get(w);

                /* output Triangle */

                result.add(Arrays.asList(
                    contour.get(a),
                    contour.get(b),
                    contour.get(c)));

                vertIndices.add(Arrays.asList(
                    verts.get(u),
                    verts.get(v),
                    verts.get(w)));

                /* remove v from the remaining polygon */

                for (int s = v, t = v + 1; t < nv; s++, t++) {
                    verts.set(s, verts.get(t));
                }
                nv--;

                /* reset error detection counter */
                count = 2 * nv;
            }
        }
    }

    /*
     * calculate area of the contour polygon
     */
    public static float TriangulateArea(final List<Vector2> contour) {
        final int n = contour.size();
        float a = 0.0F;

        for (int p = n - 1, q = 0; q < n; p = q++) {
            a += contour.get(p).getX() * contour.get(q).getY() - contour.get(q).getX() * contour.get(p).getY();
        }

        return a * 0.5F;
    }

    /*
     * Calculate area of the contour polygon
     */
    private static float area(final List<Vector2> contour) {

        final int n = contour.size();
        float a = 0.0F;

        for (int p = n - 1, q = 0; q < n; p = q++) {
            a += contour.get(p).getX() * contour.get(q).getY() - contour.get(q).getX() * contour.get(p).getY();
        }

        return a * 0.5F;
    }

    private static boolean snip(final List<Vector2> contour, final int u, final int v, final int w, final int n, final List<Integer> verts) {
        final float ax = contour.get(verts.get(u)).getX();
        final float ay = contour.get(verts.get(u)).getY();

        final float bx = contour.get(verts.get(v)).getX();
        final float by = contour.get(verts.get(v)).getY();

        final float cx = contour.get(verts.get(w)).getX();
        final float cy = contour.get(verts.get(w)).getY();

        if (EPSILON > (((bx - ax) * (cy - ay)) - ((by - ay) * (cx - ax)))) {
            return false;
        }

        for (int p = 0; p < n; p++) {
            if ((p == u) || (p == v) || (p == w)) {
                continue;
            }

            final float px = contour.get(verts.get(p)).getX();
            final float py = contour.get(verts.get(p)).getY();

            if (insideTriangle(ax, ay, bx, by, cx, cy, px, py)) {
                return false;
            }
        }
        return true;
    }

    /*
     * see if p is inside triangle abc
     */
    public static boolean insideTriangle(final float ax, final float ay,
                                         final float bx, final float by,
                                         final float cx, final float cy,
                                         final float px, final float py
    ) {
        final float aX = cx - bx;
        final float aY = cy - by;
        final float bX = ax - cx;
        final float bY = ay - cy;
        final float cX = bx - ax;
        final float cY = by - ay;
        final float apx = px - ax;
        final float apy = py - ay;
        final float bpx = px - bx;
        final float bpy = py - by;
        final float cpx = px - cx;
        final float cpy = py - cy;

        final float aCROSSbp = aX * bpy - aY * bpx;
        final float cCROSSap = cX * apy - cY * apx;
        final float bCROSScp = bX * cpy - bY * cpx;

        return (aCROSSbp >= 0.0) && (bCROSScp >= 0.0) && (cCROSSap >= 0.0);
    }
}