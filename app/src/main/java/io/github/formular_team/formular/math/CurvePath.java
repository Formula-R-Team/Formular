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

public class CurvePath extends Curve {
    private List<Curve> curves;

    private final List<CurvePath> bends;

    private List<Float> cacheLengths;

    public CurvePath() {
        this.curves = new ArrayList<>();
        this.bends = new ArrayList<>();
    }

    public List<CurvePath> getBends() {
        return this.bends;
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

    /*
     * TODO:
     * If the ending of curve is not connected to the starting
     * or the next curve, then, this is not a real path
     */
    public void checkConnection() {
    }

    public void closePath() {
        // TODO Test
        // and verify for vector3 (needs to implement equals)
        // Add a line curve if start and end of lines are not connected
        final Vector2 startPoint = this.getCurves().get(0).getPoint(0);
        final Vector2 endPoint = this.getCurves().get(this.curves.size() - 1).getPoint(1);

		if (!startPoint.equals(endPoint)) {
			this.curves.add(new LineCurve(endPoint, startPoint));
		}
    }

    @Override
    public Vector2 getPoint(final float t) {
        final float d = t * this.getLength();
        final List<Float> curveLengths = this.getCurveLengths();
        int i = 0;
        // To think about boundaries points.
        while (i < curveLengths.size()) {
            if (curveLengths.get(i) >= d) {
                final float diff = curveLengths.get(i) - d;
                final Curve curve = this.getCurves().get(i);

                final float u = 1.0F - diff / curve.getLength();

                return curve.getPointAt(u);
            }

            i++;
        }
        // loop where sum != 0, sum > d , sum+1 <d
        return null;
    }

    @Override
    public float getLength() {
        final List<Float> lens = this.getCurveLengths();
        return lens.get(lens.size() - 1);
    }

    public List<Float> getCurveLengths() {
        // We use cache values if curves and cache array are same length
		if (this.cacheLengths != null && this.cacheLengths.size() == this.curves.size()) {
			return this.cacheLengths;
		}

        // Get length of subsurve
        // Push sums into cached array
        this.cacheLengths = new ArrayList<>();
        float sums = 0.0F;
        for (int i = 0; i < this.curves.size(); i++) {
            sums += this.curves.get(i).getLength();
            this.cacheLengths.add(sums);
        }

        return this.cacheLengths;
    }

    /*
     * Returns getMin and max coordinates, as well as centroid
     */
    public Box3 getBoundingBox() {
        final List<Vector2> points = this.getPoints();

        float maxX, maxY;
        float minX, minY;

        maxX = maxY = Float.NEGATIVE_INFINITY;
        minX = minY = Float.POSITIVE_INFINITY;

        final Vector2 sum = new Vector2();
        final int il = points.size();

        for (int i = 0; i < il; i++) {
            final Vector2 p = points.get(i);

			if (p.getX() > maxX) {
				maxX = p.getX();
			} else if (p.getX() < minX) {
				minX = p.getX();
			}

			if (p.getY() > maxY) {
				maxY = p.getY();
			} else if (p.getY() < maxY) {
				minY = p.getY();
			}

            sum.add(p);
        }

        final Box3 boundingBox = new Box3();
        boundingBox.getMin().set(minX, minY, 0);
        boundingBox.getMax().set(maxX, maxY, 0);

        return boundingBox;
    }

    public void addWrapPath(final CurvePath bendpath) {
        this.bends.add(bendpath);
    }

    /*
     * http://www.planetclegg.com/projects/WarpingTextToSplines.html
     */
    protected List<Vector2> getWrapPoints(final List<Vector2> oldPts, final CurvePath path) {
        final Box3 bounds = this.getBoundingBox();
        for (int i = 0, il = oldPts.size(); i < il; i++) {
            final Vector2 p = oldPts.get(i);
            final float oldX = p.getX();
            final float oldY = p.getY();
            float xNorm = oldX / bounds.getMax().getX();
            // If using actual distance, for length > path, requires line extrusions
            //xNorm = path.getUtoTmapping(xNorm, oldX); // 3 styles. 1) wrap stretched. 2) wrap stretch by arc length 3) warp by actual distance
            xNorm = path.getUtoTmapping(xNorm, oldX);
            // check for out of bounds?
            final Vector2 pathPt = path.getPoint(xNorm);
            final Vector2 normal = path.getNormalVector(xNorm).multiply(oldY);
            p.setX(pathPt.getX() + normal.getY());
            p.setY(pathPt.getX() + normal.getY());
        }
        return oldPts;
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
        this.bends.clear();
        for (final CurvePath bend : other.bends) {
            this.bends.add(bend.clone());
        }
        this.cacheLengths = null;
        return this;
    }
}