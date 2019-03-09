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

public class Path extends CurvePath implements PathVisitor {
    private final List<Action> actions;

    public Path() {
        super();
        this.actions = new ArrayList<>();
        this.actions.add(new DummyAction());
    }

    public Path(final List<Vector2> points) {
        this();
        this.fromPoints(points);
    }

    public void visit(final PathVisitor visitor) {
        for (final Action action : this.actions) {
            action.visit(visitor);
        }
    }

    public void fromPoints(final List<Vector2> vectors) {
        this.moveTo(vectors.get(0).getX(), vectors.get(0).getY());
        for (int n = 1; n < vectors.size(); n++) {
            this.lineTo(vectors.get(n).getX(), vectors.get(n).getY());
        }
    }

    @Override
    public void moveTo(final float x, final float y) {
        this.actions.add(new MoveToAction(x, y));
    }

    @Override
    public void lineTo(final float x, final float y) {
        final Action last = this.actions.get(this.actions.size() - 1);
        final float x0 = last.getX();
        final float y0 = last.getY();
        final LineCurve curve = new LineCurve(new Vector2(x0, y0), new Vector2(x, y));
        this.add(curve);
        this.actions.add(new LineToAction(x, y));
    }

//	public void quadraticCurveTo(float aCPx, float aCPy, float aX, float aY) {
//		List<Object> lastargs = this.actions.get(this.actions.size() - 1).args;
//
//		float x0 = (Float) lastargs.get(lastargs.size() - 2);
//		float y0 = (Float) lastargs.get(lastargs.size() - 1);
//
//		QuadraticBezierCurve curve = new QuadraticBezierCurve(
//				new Vector2(x0, y0),
//				new Vector2(aCPx, aCPy),
//				new Vector2(aX, aY));
//		add(curve);
//
//		this.actions.add(new Action(PATH_ACTIONS.QUADRATIC_CURVE_TO, aCPx, aCPy, aX, aY));
//	}

    @Override
    public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
        final Action last = this.actions.get(this.actions.size() - 1);
        final float x0 = last.getX();
        final float y0 = last.getY();
        final CubicBezierCurve curve = new CubicBezierCurve(new Vector2(x0, y0),
            new Vector2(aCP1x, aCP1y),
            new Vector2(aCP2x, aCP2y),
            new Vector2(aX, aY));
        this.add(curve);
        this.actions.add(new BezierCurveToAction(aCP1x, aCP1y, aCP2x, aCP2y, aX, aY));
    }

//	public void splineThru(List<Vector2> pts) {
//		List<Object> lastargs = this.actions.get(this.actions.size() - 1).args;
//
//		float x0 = (Float) lastargs.get(lastargs.size() - 2);
//		float y0 = (Float) lastargs.get(lastargs.size() - 1);
//
//		//---
//		List<Vector2> npts = new ArrayList<Vector2>();
//		npts.add(new Vector3(x0, y0, 0));
//		npts.addAll(pts);
//
//		SplineCurve curve = new SplineCurve(npts);
//		add(curve);
//
//		this.actions.add(new Action(PATH_ACTIONS.CSPLINE_THRU, pts));
//	}

//	public void arc(float aX, float aY, float aRadius,
//					float aStartAngle, float aEndAngle, boolean aClockwise) {
//		List<Object> lastargs = this.actions.get(this.actions.size() - 1).args;
//
//		float x0 = (Float) lastargs.get(lastargs.size() - 2);
//		float y0 = (Float) lastargs.get(lastargs.size() - 1);
//
//		absarc(aX + x0, aY + y0, aRadius, aStartAngle, aEndAngle, aClockwise);
//	}

//	public void absarc(float aX, float aY, float aRadius,
//					   float aStartAngle, float aEndAngle, boolean aClockwise) {
//		absellipse(aX, aY, aRadius, aRadius, aStartAngle, aEndAngle, aClockwise);
//	}

//	public void ellipse(float aX, float aY, float xRadius, float yRadius,
//						float aStartAngle, float aEndAngle, boolean aClockwise) {
//		List<Object> lastargs = this.actions.get(this.actions.size() - 1).args;
//		float x0 = (Float) lastargs.get(lastargs.size() - 2);
//		float y0 = (Float) lastargs.get(lastargs.size() - 1);
//
//		absellipse(aX + x0, aY + y0, xRadius, yRadius, aStartAngle, aEndAngle, aClockwise);
//	}

//	public void absellipse(float aX, float aY, float xRadius, float yRadius,
//						   float aStartAngle, float aEndAngle, boolean aClockwise) {
//
//		EllipseCurve curve = new EllipseCurve(aX, aY, xRadius, yRadius,
//				aStartAngle, aEndAngle, aClockwise);
//		add(curve);
//
//		Vector2 lastPoint = curve.getPoint(aClockwise ? 1 : 0);
//
//		this.actions.add(new Action(PATH_ACTIONS.ELLIPSE, aX, aY, xRadius, yRadius, aStartAngle, aEndAngle, aClockwise, lastPoint.getX(), lastPoint.getY()));
//	}

    @Override
    public void closePath() {
        if (!this.isClosed()) {
            final Vector2 start = this.getStart();
            this.lineTo(start.x, start.y);
        }
    }

    @Override
    public List<Vector2> getSpacedPoints() {
        return this.getSpacedPoints(40);
    }

    @Override
    public List<Vector2> getSpacedPoints(final int divisions) {
        final List<Vector2> points = new ArrayList<>();
        for (int i = 0; i <= divisions; i++) {
            points.add(this.getPoint(i / (float) divisions));
        }
        return points;
    }

    public List<Vector2> getPoints(final boolean closedPath) {
        return this.getPoints(12, closedPath);
    }

    public List<Vector2> getPoints(final int divisions, final boolean closedPath) {
        final List<Vector2> points = new ArrayList<>();
        this.visit(new PathVisitor() {
            private final Vector2 last = new Vector2();

            @Override
            public void moveTo(final float x, final float y) {
                final Vector2 point = new Vector2(x, y);
                this.last.copy(point);
                points.add(point);
            }

            @Override
            public void lineTo(final float x, final float y) {
                final Vector2 point = new Vector2(x, y);
                this.last.copy(point);
                points.add(point);
            }

            @Override
            public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
                final float x0 = this.last.getX();
                final float y0 = this.last.getX();
                for (int n = 1; n <= divisions; n++) {
                    final float t = n / (float) divisions;
                    final float tx = ShapeUtils.b3(t, x0, aCP1x, aCP2x, aX);
                    final float ty = ShapeUtils.b3(t, y0, aCP1y, aCP2y, aY);
                    final Vector2 point = new Vector2(tx, ty);
                    this.last.copy(point);
                    points.add(point);
                }
            }

            @Override
            public void closePath() {}
        });
//				case QUADRATIC_CURVE_TO:
//					cpx = (Float) args.get(2);
//					cpy = (Float) args.get(3);
//					cpx1 = (Float) args.get(0);
//					cpy1 = (Float) args.get(1);
//					if (!points.isEmpty()) {
//						Vector2 laste = points.get(points.size() - 1);
//						cpx0 = laste.getX();
//						cpy0 = laste.getY();
//					} else {
//						List<Object> laste = this.actions.get(i - 1).args;
//						cpx0 = (Float) laste.get(laste.size() - 2);
//						cpy0 = (Float) laste.get(laste.size() - 1);
//					}
//					for (int j = 1; j <= divisions; j++) {
//						float t = j / (float) divisions;
//						float tx = ShapeUtils.b2(t, cpx0, cpx1, cpx);
//						float ty = ShapeUtils.b2(t, cpy0, cpy1, cpy);
//						points.add(new Vector2(tx, ty));
//					}

//				case CSPLINE_THRU:
//					List<Object> laste = this.actions.get(i - 1).args;
//					Vector2 last = new Vector2((Float) laste.get(laste.size() - 2), (Float) laste.get(laste.size() - 1));
//					List<Vector2> spts = new ArrayList<Vector2>();
//					spts.add(last);
//					List<Vector3> v = (List<Vector3>) args.get(0);
//					float n = (float)divisions * v.size();
//					spts.addAll(v);
//					SplineCurve spline = new SplineCurve(spts);
//					for (int j = 1; j <= n; j++)
//						points.add((Vector2) spline.getPointAt(j / n));
//					break;

//				case ARC:
//					float aX = (Float) args.get(0);
//					float aY = (Float) args.get(1);
//					float aRadius = (Float) args.get(2);
//					float aStartAngle = (Float) args.get(3);
//					float aEndAngle = (Float) args.get(4);
//					boolean aClockwise = !!(Boolean) args.get(5);
//					float deltaAngle = aEndAngle - aStartAngle;
//					int tdivisions = divisions * 2;
//					for (int j = 1; j <= tdivisions; j++) {
//						float t = j / (float) tdivisions;
//						if (!aClockwise) {
//							t = 1.0F - t;
//						}
//						float angle = aStartAngle + t * deltaAngle;
//						float tx = aX + aRadius * Mth.cos(angle);
//						float ty = aY + aRadius * Mth.sin(angle);
//						points.add(new Vector2(tx, ty));
//					}

//				case ELLIPSE:
//					float aXE = (Float) args.get(0);
//					float aYE = (Float) args.get(1);
//					float xRadiusE = (Float) args.get(2);
//					float yRadiusE = (Float) args.get(3);
//					float aStartAngleE = (Float) args.get(4);
//					float aEndAngleE = (Float) args.get(5);
//					boolean aClockwiseE = !!(Boolean) args.get(6);
//					float deltaAngleE = aEndAngleE - aStartAngleE;
//					float tdivisionsE = divisions * 2.0F;
//					for (int j = 1; j <= tdivisionsE; j++) {
//						float t = j / tdivisionsE;
//						if (!aClockwiseE) {
//							t = 1.0F - t;
//						}
//						float angle = aStartAngleE + t * deltaAngleE;
//						float tx = aXE + xRadiusE * Mth.cos(angle);
//						float ty = aYE + yRadiusE * Mth.sin(angle);
//						points.add(new Vector2(tx, ty));
//					}
        if (points.size() >= 2) {
            final Vector2 lastPoint = points.get(points.size() - 1);
            final float epsilon = 0.0000001F;
            if (Math.abs(lastPoint.getX() - points.get(0).getX()) < epsilon &&
                Math.abs(lastPoint.getY() - points.get(0).getY()) < epsilon) {
                points.remove(points.size() - 1);
            }
        }
        if (closedPath) {
            points.add(points.get(0));
        }
        return points;
    }

    public List<Shape> toShapes() {
        return this.toShapes(false, false);
    }

    public List<Shape> toShapes(final boolean isCCW, final boolean noHoles) {
        final List<Shape> shapes = new ArrayList<>();
        final List<Path> subPaths = this.extractSubPaths();
        if (subPaths.isEmpty()) {
            return shapes;
        }
        if (noHoles) {
            return this.toShapesNoHoles(subPaths);
        }
        if (subPaths.size() == 1) {
            shapes.add(new Shape().copy(subPaths.get(0)));
            return shapes;
        }
        boolean holesFirst = !ShapeUtils.isClockWise(subPaths.get(0).getPoints());
        holesFirst = isCCW != holesFirst;
        final class ShapeHole {
            private final Shape s;

            private final List<Vector2> p;

            private ShapeHole(final Shape s, final List<Vector2> p) {
                this.s = s;
                this.p = p;
            }
        }

        final class PathHole {
            private final Path h;

            private final Vector2 p;

            private PathHole(final Path h, final Vector2 p) {
                this.h = h;
                this.p = p;
            }
        }
        final List<ShapeHole> newShapes = new ArrayList<>();
        final List<List<PathHole>> betterShapeHoles = new ArrayList<>();
        List<List<PathHole>> newShapeHoles = new ArrayList<>();
        newShapeHoles.add(new ArrayList<>());
        int mainIdx = 0;
        for (int i = 0, l = subPaths.size(); i < l; i++) {
            final Path tmpPath = subPaths.get(i);
            final List<Vector2> tmpPoints = tmpPath.getPoints();
            boolean solid = ShapeUtils.isClockWise(tmpPoints);
            solid = isCCW != solid;
            if (solid) {
                if ((!holesFirst) && (mainIdx < newShapes.size() && newShapes.get(mainIdx) != null)) {
                    mainIdx++;
                }
                final Shape s = new Shape().copy(tmpPath);
                newShapes.add(mainIdx, new ShapeHole(s, tmpPoints));
                if (holesFirst) {
                    mainIdx++;
                }
                newShapeHoles.add(mainIdx, new ArrayList<>());
            } else {
                newShapeHoles.get(mainIdx).add(new PathHole(tmpPath, tmpPoints.get(0)));
            }

        }
        // only Holes? -> probably all Shapes with wrong orientation
        if (newShapes.get(0) == null) {
            return this.toShapesNoHoles(subPaths);
        }
        if (newShapes.size() > 1) {
            boolean ambiguous = false;
            final List<Integer> toChange = new ArrayList<>();
            for (int sIdx = 0, sLen = newShapes.size(); sIdx < sLen; sIdx++) {
                betterShapeHoles.add(new ArrayList<>());
            }
            for (int sIdx = 0, sLen = newShapes.size(); sIdx < sLen; sIdx++) {
                final List<PathHole> sho = newShapeHoles.get(sIdx);
                for (int hIdx = 0; hIdx < sho.size(); hIdx++) {
                    final PathHole ho = sho.get(hIdx);
                    boolean holeUnassigned = true;
                    for (int s2Idx = 0; s2Idx < newShapes.size(); s2Idx++) {
                        if (this.isPointInsidePolygon(ho.p, newShapes.get(s2Idx).p)) {
                            if (sIdx != s2Idx) {
                                toChange.add(hIdx);
                            }
                            if (holeUnassigned) {
                                holeUnassigned = false;
                                betterShapeHoles.get(s2Idx).add(ho);
                            } else {
                                ambiguous = true;
                            }
                        }
                    }
                    if (holeUnassigned) {
                        betterShapeHoles.get(sIdx).add(ho);
                    }
                }
            }
            if (!toChange.isEmpty() && !ambiguous) {
                newShapeHoles = betterShapeHoles;
            }
        }
        for (int i = 0, il = newShapes.size(); i < il; i++) {
            final Shape tmpShape = newShapes.get(i).s;
            shapes.add(tmpShape);
            final List<PathHole> tmpHoles = newShapeHoles.get(i);
            for (int j = 0, jl = tmpHoles.size(); j < jl; j++) {
                tmpShape.getHoles().add(tmpHoles.get(j).h);
            }
        }
        return shapes;
    }

    private List<Path> extractSubPaths() {
        final List<Path> subPaths = new ArrayList<>();
        final class Extractor implements PathVisitor {
            private Path currentPath = new Path();

            @Override
            public void moveTo(final float x, final float y) {
                this.emit();
                this.currentPath = new Path();
            }

            @Override
            public void lineTo(final float x, final float y) {
                this.currentPath.lineTo(x, y);
            }

            @Override
            public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
                this.currentPath.bezierCurveTo(aCP1x, aCP1y, aCP2x, aCP2y, aX, aY);
            }

            @Override
            public void closePath() {}

            private void emit() {
                if (!this.currentPath.getCurves().isEmpty()) {
                    subPaths.add(this.currentPath);
                }
            }
        }
        final Extractor e = new Extractor();
        this.visit(e);
        e.emit();
        return subPaths;
    }

    private List<Shape> toShapesNoHoles(final List<Path> inSubpaths) {
        final List<Shape> shapes = new ArrayList<>();
        for (int i = 0; i < inSubpaths.size(); i++) {
            shapes.add(new Shape().copy(inSubpaths.get(i)));
        }
        return shapes;
    }

    private boolean isPointInsidePolygon(final Vector2 inPt, final List<Vector2> inPolygon) {
        final int polyLen = inPolygon.size();
        // inPt on polygon contour => immediate success or
        // toggling of inside/outside at every single! intersection point of an edge
        //  with the horizontal line through inPt, left of inPt
        //  not counting lowerY endpoints of edges and whole edges on that line
        boolean inside = false;
        for (int p = polyLen - 1, q = 0; q < polyLen; p = q++) {
            Vector2 edgeLowPt = inPolygon.get(p);
            Vector2 edgeHighPt = inPolygon.get(q);
            float edgeDx = edgeHighPt.getX() - edgeLowPt.getX();
            float edgeDy = edgeHighPt.getY() - edgeLowPt.getY();
            if (Math.abs(edgeDy) > Float.MIN_VALUE) {
                // not parallel
                if (edgeDy < 0) {
                    edgeLowPt = inPolygon.get(q);
                    edgeDx = -edgeDx;
                    edgeHighPt = inPolygon.get(p);
                    edgeDy = -edgeDy;
                }
                if ((inPt.getY() < edgeLowPt.getY()) || (inPt.getY() > edgeHighPt.getY())) {
                    continue;
                }
                if (inPt.getY() == edgeLowPt.getY()) {
                    if (inPt.getX() == edgeLowPt.getX()) {
                        // inPt is on contour ?
                        return true;
                    }
                    // continue;
                    // no intersection or edgeLowPt => doesn't count !!!
                } else {

                    final float perpEdge = edgeDy * (inPt.getX() - edgeLowPt.getX()) - edgeDx * (inPt.getY() - edgeLowPt.getY());
                    if (perpEdge == 0) {
                        // inPt is on contour ?
                        return true;
                    }
                    if (perpEdge < 0) {
                        continue;
                    }
                    // true intersection left of inPt
                    inside = !inside;
                }
            } else {
                // parallel or collinear
                if (inPt.getY() != edgeLowPt.getY()) {
                    // parallel
                    continue;
                }
                // edge lies on the same horizontal line as inPt
                if (((edgeHighPt.getX() <= inPt.getX()) && (inPt.getX() <= edgeLowPt.getX())) ||
                    ((edgeLowPt.getX() <= inPt.getX()) && (inPt.getX() <= edgeHighPt.getX()))) {
                    // inPt: Point on contour !
                    return true;
                }
            }
        }
        return inside;
    }

//    public List<Vector2> getTransformedSpacedPoints(final boolean closedPath) {
//        return this.getTransformedSpacedPoints(closedPath, this.getBends());
//    }
//
//    public List<Vector2> getTransformedSpacedPoints(final boolean closedPath, final List<CurvePath> bends) {
//        List<Vector2> oldPts = this.getSpacedPoints();
//        for (int i = 0; i < bends.size(); i++) {
//            oldPts = this.getWrapPoints(oldPts, bends.get(i));
//        }
//        return oldPts;
//    }

//	public Geometry createPointsGeometry()
//	{
//		return this.createGeometry(this.getPoints(true) );
//	}
//
//	public Geometry createPointsGeometry(final int divisions )
//	{
//		return this.createGeometry(this.getPoints( divisions, true ) );
//	}
//
//	public Geometry createSpacedPointsGeometry()
//	{
//		return this.createGeometry(this.getSpacedPoints(true) );
//	}
//
//	public Geometry createSpacedPointsGeometry(final int divisions )
//	{
//		return this.createGeometry(this.getSpacedPoints( divisions, true ) );
//	}
//
//	private Geometry createGeometry(final List<Vector2> points)
//	{
//		final Geometry geometry = new Geometry();
//
//		for ( int i = 0; i < points.size(); i ++ )
//		{
//			geometry.getVertices().add( new Vector3(
//					points.get( i ).getX(),
//					points.get( i ).getY(), 0 ) );
//		}
//
//		return geometry;
//	}

//    public List<Vector2> getTransformedPoints() {
//        return this.getTransformedPoints(false, this.getBends());
//    }
//
//    public List<Vector2> getTransformedPoints(final boolean closedPath) {
//        return this.getTransformedPoints(closedPath, this.getBends());
//    }
//
//    public List<Vector2> getTransformedPoints(final boolean closedPath, final List<CurvePath> bends) {
//        List<Vector2> oldPts = this.getPoints(closedPath);
//        for (int i = 0; i < bends.size(); i++) {
//            oldPts = this.getWrapPoints(oldPts, bends.get(i));
//        }
//        return oldPts;
//    }

    @Override
    public Path clone() {
        return new Path().copy(this);
    }

    public Path copy(final Path other) {
        super.copy(other);
        this.actions.subList(1, this.actions.size()).clear();
        other.visit(this);
        return this;
    }

    private interface Action {
        float getX();

        float getY();

        void visit(final PathVisitor visitor);
    }

    private final class DummyAction implements Action {
        @Override
        public float getY() {
            return 0.0F;
        }

        @Override
        public float getX() {
            return 0.0F;
        }

        @Override
        public void visit(final PathVisitor visitor) {}
    }

    private final class MoveToAction implements Action {
        private final float x;

        private final float y;

        private MoveToAction(final float x, final float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public float getX() {
            return this.x;
        }

        @Override
        public float getY() {
            return this.y;
        }

        @Override
        public void visit(final PathVisitor visitor) {
            visitor.moveTo(this.x, this.y);
        }
    }

    private final class LineToAction implements Action {
        private final float x;

        private final float y;

        private LineToAction(final float x, final float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public float getX() {
            return this.x;
        }

        @Override
        public float getY() {
            return this.y;
        }

        @Override
        public void visit(final PathVisitor visitor) {
            visitor.lineTo(this.x, this.y);
        }
    }

    private final class BezierCurveToAction implements Action {
        private final float aCP1x;

        private final float aCP1y;

        private final float aCP2x;

        private final float aCP2y;

        private final float aX;

        private final float aY;

        private BezierCurveToAction(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
            this.aCP1x = aCP1x;
            this.aCP1y = aCP1y;
            this.aCP2x = aCP2x;
            this.aCP2y = aCP2y;
            this.aX = aX;
            this.aY = aY;
        }

        @Override
        public float getX() {
            return this.aX;
        }

        @Override
        public float getY() {
            return this.aY;
        }

        @Override
        public void visit(final PathVisitor visitor) {
            visitor.bezierCurveTo(this.aCP1x, this.aCP1y, this.aCP2x, this.aCP2y, this.aX, this.aY);
        }
    }
}
