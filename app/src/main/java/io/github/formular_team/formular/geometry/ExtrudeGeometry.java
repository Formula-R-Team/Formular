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
package io.github.formular_team.formular.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.formular_team.formular.math.Color;
import io.github.formular_team.formular.math.Curve;
import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.ShapeUtils;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;

public class ExtrudeGeometry extends Geometry {
    public static class ExtrudeGeometryParameters {
        // size of the text
        public float size;

        // thickness to extrude text
        public float height;

        // number of points on the curves
        public int curveSegments = 12;

        // number of points for z-side extrusions / used for subdividing segments of extrude spline too
        public int steps = 1;

        // Amount
        public int amount = 100;

        // turn on bevel
        public boolean bevelEnabled = true;

        // how deep into text bevel goes
        public float bevelThickness = 6;

        // how far from text outline is bevel
        public float bevelSize = this.bevelThickness - 2;

        // number of bevel layers
        public int bevelSegments = 3;

        // 2d/3d spline path to extrude shape orthogonality to
        public Curve extrudePath;

        public UVGenerator uvGenerator = new WorldUVGenerator(new Matrix4());

        // material index for front and back faces
        public int material;

        // material index for extrusion and beveled faces
        int extrudeMaterial;
    }

    private static final Vector2 __v1 = new Vector2();

    private static final Vector2 __v2 = new Vector2();

    private static final Vector2 __v3 = new Vector2();

    private static final Vector2 __v4 = new Vector2();

    private static final Vector2 __v5 = new Vector2();

    private static final Vector2 __v6 = new Vector2();

    private List<List<Vector2>> holes;

    private List<List<Integer>> localFaces;

    private final ExtrudeGeometryParameters options;

    private int shapesOffset;

    private int verticesCount;

    public ExtrudeGeometry(final ExtrudeGeometryParameters options) {
        this(new ArrayList<>(), options);
    }

    public ExtrudeGeometry(final Shape shape, final ExtrudeGeometryParameters options) {
        this(Collections.singletonList(shape), options);
    }

    public ExtrudeGeometry(final List<Shape> shapes, final ExtrudeGeometryParameters options) {
        super();
        this.options = options;
        this.addShape(shapes, options);
        this.computeFaceNormals();
    }

    public void addShape(final List<Shape> shapes, final ExtrudeGeometryParameters options) {
        for (int s = 0; s < shapes.size(); s++) {
            this.addShape(shapes.get(s), options);
        }
    }

    public void addShape(final Shape shape, final ExtrudeGeometryParameters options) {
        List<Vector2> extrudePts = null;
        boolean extrudeByPath = false;

        final Vector3 binormal = new Vector3();
        final Vector3 normal = new Vector3();
        final Vector3 position2 = new Vector3();

        FrenetFrames splineTube = null;

        if (options.extrudePath != null) {
            extrudePts = options.extrudePath.getSpacedPoints(options.steps);
            extrudeByPath = true;
            options.bevelEnabled = false;
            splineTube = new FrenetFrames(options.extrudePath, options.steps, options.extrudePath.isClosed());
        }
        if (!options.bevelEnabled) {
            options.bevelSegments = 0;
            options.bevelThickness = 0.0F;
            options.bevelSize = 0.0F;
        }

        this.shapesOffset = this.getVertices().size();

        final List<Vector2> vertices = shape.getPoints(false);

        this.holes = shape.getPointsHoles();

        final boolean reverse = !ShapeUtils.isClockWise(vertices);

        if (reverse) {
            Collections.reverse(vertices);

            // Maybe we should also check if holes are in the opposite direction, just to be safe ...
            for (int h = 0, hl = this.holes.size(); h < hl; h++) {
                final List<Vector2> ahole = this.holes.get(h);

                if (ShapeUtils.isClockWise(ahole)) {
                    Collections.reverse(ahole);
                }
            }

            // If vertices are in order now, we shouldn't need to worry about them again (hopefully)!
        }

        this.localFaces = ShapeUtils.triangulateShape(vertices, this.holes);

        // vertices has all points but contour has only points of circumference
        final List<Vector2> contour = new ArrayList<>(vertices);

        for (int h = 0, hl = this.holes.size(); h < hl; h++) {
            vertices.addAll(this.holes.get(h));
        }
        this.verticesCount = vertices.size();
        //
        // Find directions for point movement
        //
        final List<Vector2> contourMovements = new ArrayList<>();
        for (int i = 0, il = contour.size(), j = il - 1, k = i + 1; i < il; i++, j++, k++) {
            if (j == il) {
                j = 0;
            }
            if (k == il) {
                k = 0;
            }

            contourMovements.add(
                this.getBevelVec(contour.get(i), contour.get(j), contour.get(k)));
        }

        final List<List<Vector2>> holesMovements = new ArrayList<>();
        final List<Vector2> verticesMovements = new ArrayList<>(contourMovements);

        for (int h = 0, hl = this.holes.size(); h < hl; h++) {
            final List<Vector2> ahole = this.holes.get(h);

            final List<Vector2> oneHoleMovements = new ArrayList<>();

            for (int i = 0, il = ahole.size(), j = il - 1, k = i + 1; i < il; i++, j++, k++) {
                if (j == il) {
                    j = 0;
                }
                if (k == il) {
                    k = 0;
                }

                //  (j)---(i)---(k)
                oneHoleMovements.add(this.getBevelVec(ahole.get(i), ahole.get(j), ahole.get(k)));
            }

            holesMovements.add(oneHoleMovements);
            verticesMovements.addAll(oneHoleMovements);
        }


        // Loop bevelSegments, 1 for the front, 1 for the back
        for (int b = 0; b < options.bevelSegments; b++) {
            final float t = b / (float) options.bevelSegments;
            final float z = options.bevelThickness * (1.0F - t);

            final float bs = options.bevelSize * (Mth.sin(t * Mth.PI / 2.0F)); // curved
            //bs = bevelSize * t ; // linear

            // contract shape

            for (int i = 0, il = contour.size(); i < il; i++) {
                final Vector2 vert = this.scalePt2(contour.get(i), contourMovements.get(i), bs);
                this.v(vert.getX(), vert.getY(), -z);
            }

            // expand holes
            for (int h = 0, hl = this.holes.size(); h < hl; h++) {
                final List<Vector2> ahole = this.holes.get(h);
                final List<Vector2> oneHoleMovements = holesMovements.get(h);

                for (int i = 0, il = ahole.size(); i < il; i++) {
                    final Vector2 vert = this.scalePt2(ahole.get(i), oneHoleMovements.get(i), bs);
                    this.v(vert.getX(), vert.getY(), -z);
                }
            }
        }


        // Back facing vertices

        for (int i = 0; i < vertices.size(); i++) {
            final Vector2 vert = options.bevelEnabled
                ? this.scalePt2(vertices.get(i), verticesMovements.get(i), options.bevelSize)
                : vertices.get(i);

            if (!extrudeByPath) {
                this.v(vert.getX(), vert.getY(), 0);
            } else {
                normal.copy(splineTube.getNormals().get(0)).multiply(vert.getX());
                binormal.copy(splineTube.getBinormals().get(0)).multiply(vert.getY());
                position2.copy(extrudePts.get(0)).add(normal).add(binormal);

                this.v(position2.getX(), position2.getY(), position2.getZ());
            }
        }

        // Add stepped vertices...
        // Including front facing vertices

        for (int s = 1; s <= options.steps; s++) {
            for (int i = 0; i < vertices.size(); i++) {
                final Vector2 vert = options.bevelEnabled
                    ? this.scalePt2(vertices.get(i), verticesMovements.get(i), options.bevelSize)
                    : vertices.get(i);

                if (!extrudeByPath) {
                    this.v(vert.getX(), vert.getY(), (float) options.amount / options.steps * s);
                } else {
                    normal.copy(splineTube.getNormals().get(s)).multiply(vert.getX());
                    binormal.copy(splineTube.getBinormals().get(s)).multiply(vert.getY());

                    position2.copy(extrudePts.get(s)).add(normal).add(binormal);

                    this.v(position2.getX(), position2.getY(), position2.getZ());
                }
            }
        }


        // Add bevel segments planes
        for (int b = options.bevelSegments - 1; b >= 0; b--) {
            final float t = (float) b / options.bevelSegments;
            final float z = options.bevelThickness * (1 - t);

            final float bs = options.bevelSize * Mth.sin(t * Mth.PI / 2.0F);

            // contract shape
            for (int i = 0, il = contour.size(); i < il; i++) {
                final Vector2 vert = this.scalePt2(contour.get(i), contourMovements.get(i), bs);
                this.v(vert.getX(), vert.getY(), options.amount + z);
            }

            // expand holes
            for (int h = 0, hl = this.holes.size(); h < hl; h++) {
                final List<Vector2> ahole = this.holes.get(h);
                final List<Vector2> oneHoleMovements = holesMovements.get(h);

                for (int i = 0, il = ahole.size(); i < il; i++) {
                    final Vector2 vert = this.scalePt2(ahole.get(i), oneHoleMovements.get(i), bs);

                    if (!extrudeByPath) {
                        this.v(vert.getX(), vert.getY(), options.amount + z);
                    } else {
                        this.v(vert.getX(),
                            vert.getY() + extrudePts.get(options.steps - 1).getY(),
                            extrudePts.get(options.steps - 1).getX() + z);
                    }
                }
            }
        }

        //
        // Handle Faces
        //

        // Top and bottom faces
        this.buildLidFaces();

        // Sides faces
        this.buildSideFaces(contour);
    }

    private Vector2 getBevelVec(final Vector2 pt_i, final Vector2 pt_j, final Vector2 pt_k) {
        // Algorithm 2
        return this.getBevelVec2(pt_i, pt_j, pt_k);
    }

    private Vector2 getBevelVec1(final Vector2 pt_i, final Vector2 pt_j, final Vector2 pt_k) {
        final float anglea = Mth.atan2(pt_j.getY() - pt_i.getY(), pt_j.getX() - pt_i.getX());
        float angleb = Mth.atan2(pt_k.getY() - pt_i.getY(), pt_k.getX() - pt_i.getX());

        if (anglea > angleb) {
            angleb += Mth.PI * 2.0F;
        }

        final float anglec = (anglea + angleb) / 2.0F;

        final float x = -Mth.cos(anglec);
        final float y = -Mth.sin(anglec);

        return new Vector2(x, y);
    }

    private Vector2 scalePt2(final Vector2 pt, final Vector2 vec, final float size) {
        return vec.clone().multiply(size).add(pt);
    }

    /*
     * good reading for line-line intersection
     * http://sputsoft.com/blog/2010/03/line-line-intersection.html
     */
    private Vector2 getBevelVec2(final Vector2 pt_i, final Vector2 pt_j, final Vector2 pt_k) {
        final Vector2 a = ExtrudeGeometry.__v1;
        final Vector2 b = ExtrudeGeometry.__v2;
        final Vector2 v_hat = ExtrudeGeometry.__v3;
        final Vector2 w_hat = ExtrudeGeometry.__v4;
        final Vector2 p = ExtrudeGeometry.__v5;
        final Vector2 q = ExtrudeGeometry.__v6;

        // define a as vector j->i
        // define b as vectot k->i
        a.set(pt_i.getX() - pt_j.getX(), pt_i.getY() - pt_j.getY());
        b.set(pt_i.getX() - pt_k.getX(), pt_i.getY() - pt_k.getY());

        // get unit vectors
        final Vector2 v = a.normalize();
        final Vector2 w = b.normalize();

        // normals from pt i
        v_hat.set(-v.getY(), v.getX());
        w_hat.set(w.getY(), -w.getX());

        // pts from i
        p.copy(pt_i).add(v_hat);
        q.copy(pt_i).add(w_hat);

        if (p.equals(q)) {
            return w_hat.clone();
        }

        // Points from j, k. helps prevents points cross overover most of the time
        p.copy(pt_j).add(v_hat);
        q.copy(pt_k).add(w_hat);

        final float v_dot_w_hat = v.dot(w_hat);
        final float q_sub_p_dot_w_hat = q.sub(p).dot(w_hat);

        // We should not reach these conditions

        if (v_dot_w_hat == 0) {
//            Log.warn("ExtrudeGeometry.getBevelVec2() Either infinite or no solutions!");

            if (q_sub_p_dot_w_hat == 0) {
//                Log.warn("ExtrudeGeometry.getBevelVec2() Its finite solutions.");
            } else {
//                Log.warn("ExtrudeGeometry.getBevelVec2() Too bad, no solutions.");
            }
        }

        final float s = q_sub_p_dot_w_hat / v_dot_w_hat;

        // in case of emergency, revert to algorithm 1.
        if (s < 0) {
            return this.getBevelVec1(pt_i, pt_j, pt_k);
        }

        final Vector2 intersection = v.multiply(s).add(p);

        // Don't normalize!, otherwise sharp corners become ugly
        return intersection.sub(pt_i).clone();
    }

    private void buildLidFaces() {
        final int flen = this.localFaces.size();
//        Log.debug( "ExtrudeGeometry.buildLidFaces() faces=" + flen);

        if (this.options.bevelEnabled) {
            int layer = 0; // steps + 1
            int offset = this.shapesOffset * layer;

            // Bottom faces

            for (int i = 0; i < flen; i++) {
                final List<Integer> face = this.localFaces.get(i);
                this.f3(face.get(2) + offset, face.get(1) + offset, face.get(0) + offset, true);
            }

            layer = this.options.steps + this.options.bevelSegments * 2;
            offset = this.verticesCount * layer;

            // Top faces

            for (int i = 0; i < flen; i++) {
                final List<Integer> face = this.localFaces.get(i);
                this.f3(face.get(0) + offset, face.get(1) + offset, face.get(2) + offset, false);
            }
        } else {
            // Bottom faces

            for (int i = 0; i < flen; i++) {
                final List<Integer> face = this.localFaces.get(i);
                this.f3(face.get(2), face.get(1), face.get(0), true);
            }

            // Top faces

            for (int i = 0; i < flen; i++) {
                final List<Integer> face = this.localFaces.get(i);
                this.f3(face.get(0) + this.verticesCount * this.options.steps,
                    face.get(1) + this.verticesCount * this.options.steps,
                    face.get(2) + this.verticesCount * this.options.steps, false);
            }
        }
    }

    // Create faces for the z-sides of the shape

    private void buildSideFaces(final List<Vector2> contour) {
        int layeroffset = 0;
        this.sidewalls(contour, layeroffset);
        layeroffset += contour.size();

        for (int h = 0; h < this.holes.size(); h++) {
            final List<Vector2> ahole = this.holes.get(h);
            this.sidewalls(ahole, layeroffset);
            layeroffset += ahole.size();
        }
    }

    private void sidewalls(final List<Vector2> contour, final int layeroffset) {
        int i = contour.size();
        while (--i >= 0) {
            int k = i - 1;
            if (k < 0) {
                k = contour.size() - 1;
            }
            final int sl = this.options.steps + this.options.bevelSegments * 2;
            for (int s = 0; s < sl; s++) {
                final int slen1 = this.verticesCount * s;
                final int slen2 = this.verticesCount * (s + 1);
                final int a = layeroffset + i + slen1;
                final int b = layeroffset + k + slen1;
                final int c = layeroffset + k + slen2;
                final int d = layeroffset + i + slen2;
                this.f4(a, b, c, d);
            }
        }
    }


    private void v(final float x, final float y, final float z) {
        this.getVertices().add(new Vector3(x, y, z));
    }

    private void f3(int a, int b, int c, final boolean isBottom) {
        a += this.shapesOffset;
        b += this.shapesOffset;
        c += this.shapesOffset;

        // normal, color, material
        this.getFaces().add(new Face3(a, b, c, this.options.material));

        final List<Vector2> uvs = isBottom
            ? this.options.uvGenerator.generateBottomUV(this, a, b, c)
            : this.options.uvGenerator.generateTopUV(this, a, b, c);

        this.getFaceVertexUvs().get(0).add(uvs);
    }

    private void f4(int a, int b, int c, int d) {
        a += this.shapesOffset;
        b += this.shapesOffset;
        c += this.shapesOffset;
        d += this.shapesOffset;

        final List<Color> colors = new ArrayList<>();
        final List<Vector3> normals = new ArrayList<>();
        this.getFaces().add(new Face3(a, b, d, normals, colors, this.options.extrudeMaterial));
        final List<Color> colors2 = new ArrayList<>();
        final List<Vector3> normals2 = new ArrayList<>();
        this.getFaces().add(new Face3(b, c, d, normals2, colors2, this.options.extrudeMaterial));

        final List<Vector2> uvs = this.options.uvGenerator.generateSideWallUV(this, a, b, c, d);
        this.getFaceVertexUvs().get(0).add(Arrays.asList(uvs.get(0), uvs.get(1), uvs.get(3)));
        this.getFaceVertexUvs().get(0).add(Arrays.asList(uvs.get(1), uvs.get(2), uvs.get(3)));

    }

    public interface UVGenerator {
        List<Vector2> generateTopUV(final Geometry geometry, final int indexA, final int indexB, final int indexC);

        default List<Vector2> generateBottomUV(final ExtrudeGeometry geometry, final int indexA, final int indexB, final int indexC) {
            return this.generateTopUV(geometry, indexA, indexB, indexC);
        }

        List<Vector2> generateSideWallUV(final Geometry geometry, final int indexA, final int indexB, final int indexC, final int indexD);
    }

    public static final class WorldUVGenerator implements UVGenerator {
        private final Matrix4 transform;

        private final Vector3 vertex = new Vector3();

        public WorldUVGenerator(final Matrix4 transform) {
            this.transform = transform;
        }

        private void transform(final Vector3 v) {
            this.vertex.copy(v).apply(this.transform);
        }

        @Override
        public List<Vector2> generateTopUV(final Geometry geometry, final int indexA, final int indexB, final int indexC) {
            this.transform(geometry.getVertices().get(indexA));
            final float ax = this.vertex.getX();
            final float ay = this.vertex.getY();
            this.transform(geometry.getVertices().get(indexB));
            final float bx = this.vertex.getX();
            final float by = this.vertex.getY();
            this.transform(geometry.getVertices().get(indexC));
            final float cx = this.vertex.getX();
            final float cy = this.vertex.getY();
            return Arrays.asList(
                new Vector2(ax, ay),
                new Vector2(bx, by),
                new Vector2(cx, cy)
            );
        }

        @Override
        public List<Vector2> generateSideWallUV(final Geometry geometry, final int indexA, final int indexB, final int indexC, final int indexD) {
            this.transform(geometry.getVertices().get(indexA));
            final float ax = this.vertex.getX();
            final float ay = this.vertex.getY();
            final float az = this.vertex.getZ();
            this.transform(geometry.getVertices().get(indexB));
            final float bx = this.vertex.getX();
            final float by = this.vertex.getY();
            final float bz = this.vertex.getZ();
            this.transform(geometry.getVertices().get(indexC));
            final float cx = this.vertex.getX();
            final float cy = this.vertex.getY();
            final float cz = this.vertex.getZ();
            this.transform(geometry.getVertices().get(indexD));
            final float dx = this.vertex.getX();
            final float dy = this.vertex.getY();
            final float dz = this.vertex.getZ();
            if (Math.abs(ay - by) < 1.0e-6F) {
                return Arrays.asList(
                    new Vector2(ax, az),
                    new Vector2(bx, bz),
                    new Vector2(cx, cz),
                    new Vector2(dx, dz)
                );
            }
            return Arrays.asList(
                new Vector2(ay, az),
                new Vector2(by, bz),
                new Vector2(cy, cz),
                new Vector2(dy, dz)
            );
        }
    }
}