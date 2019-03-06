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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.formular_team.formular.math.Box3;
import io.github.formular_team.formular.math.Color;
import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Sphere;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;

/**
 * Base class for geometries. A geometry holds all data necessary to describe a 3D model.
 *
 * <pre>
 * {@code
 * Geometry geometry = new Geometry();
 *
 * geometry.getVertices().add( new Vector3( -10, 10, 0 ) );
 * geometry.getVertices().add( new Vector3( -10, -10, 0 ) );
 * geometry.getVertices().add( new Vector3( 10, -10, 0 ) );
 * geometry.getFaces().add( new Face3( 0, 1, 2 ) );
 *
 * geometry.computeBoundingSphere();
 * }
 * </pre>
 *
 * @author thothbot
 */
public class Geometry extends AbstractGeometry {
    // Array of vertices.
    private List<Vector3> vertices;

    // one-to-one vertex colors, used in ParticleSystem, Line and Ribbon
    private List<Color> colors;

    private List<Face3> faces;

    private List<List<List<Vector2>>> faceVertexUvs;

    /*
     * An array containing distances between vertices for Line geometries.
     * This is required for LinePieces/LineDashedMaterial to render correctly.
     * Line distances can also be generated with computeLineDistances.
     */
    private List<Float> lineDistances;

    /*
     * True if geometry has tangents. Set in Geometry.computeTangents.
     */
    private boolean hasTangents = false;

    /*
     * The intermediate typed arrays will be deleted when set to false
     */
    private boolean dynamic = true;

    public Geometry() {
        super();

        this.vertices = new ArrayList<>();
        this.colors = new ArrayList<>();

        this.faces = new ArrayList<>();

        this.faceVertexUvs = new ArrayList<>();
        this.faceVertexUvs.add(new ArrayList<>());

        this.lineDistances = new ArrayList<>();
    }

    /**
     * Set to true if attribute buffers will need to change in runtime (using "dirty" flags).
     * Unless set to true internal typed arrays corresponding to buffers will be deleted once sent to GPU.
     * Defaults to true.
     *
     * @return
     */
    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(final boolean dynamic) {
        this.dynamic = dynamic;
    }


    public List<Float> getLineDistances() {
        return this.lineDistances;
    }

    public void setColors(final List<Color> colors) {
        this.colors = colors;
    }

    public List<Color> getColors() {
        return this.colors;
    }

    public void setFaces(final List<Face3> faces) {
        this.faces = faces;
    }

    /**
     * Gets the List of triangles: {@link Face3}. The array of faces describe how each vertex in the model is connected with each other.
     * To signal an update in this array, Geometry.elementsNeedUpdate needs to be set to true.
     */
    public List<Face3> getFaces() {
        return this.faces;
    }

    public void setVertices(final List<Vector3> vertices) {
        this.vertices = vertices;
    }

    /**
     * Gets List of {@link Vector3}. The array of vertices holds every position of points in the model.
     * To signal an update in this array, Geometry.verticesNeedUpdate needs to be set to true.
     */
    public List<Vector3> getVertices() {
        return this.vertices;
    }

    /**
     * Gets the List of face UV layers.
     * Each UV layer is an array of UVs matching the order and number of vertices in faces.
     * To signal an update in this array, Geometry.uvsNeedUpdate needs to be set to true.
     */
    public List<List<List<Vector2>>> getFaceVertexUvs() {
        return this.faceVertexUvs;
    }

    public void setFaceVertexUvs(final List<List<List<Vector2>>> faceVertexUvs) {
        this.faceVertexUvs = faceVertexUvs;
    }

    /**
     * @return the hasTangents
     */
    public boolean isHasTangents() {
        return this.hasTangents;
    }

    /**
     * @param hasTangents the hasTangents to set
     */
    public void setHasTangents(final boolean hasTangents) {
        this.hasTangents = hasTangents;
    }

    /**
     * Bakes matrix transform directly into vertex coordinates.
     */
    public void applyMatrix(final Matrix4 matrix) {
        final Matrix3 normalMatrix = new Matrix3().getNormalMatrix(matrix);

        for (int i = 0, il = this.vertices.size(); i < il; i++) {

            final Vector3 vertex = this.vertices.get(i);
            vertex.apply(matrix);

        }

        for (int i = 0, il = this.faces.size(); i < il; i++) {

            final Face3 face = this.faces.get(i);
            face.normal.apply(normalMatrix).normalize();

            for (int j = 0, jl = face.vertexNormals.size(); j < jl; j++) {

                face.vertexNormals.get(j).apply(normalMatrix).normalize();

            }

        }

        this.computeBoundingBox();

        this.computeBoundingSphere();
    }

//	public Geometry fromBufferGeometry( BufferGeometry geometry )
//	{
//
//		Float32Array vertices = (Float32Array)geometry.getAttribute("position").getArray();
//		Uint16Array indices = geometry.getAttribute("index") != null
//				? (Uint16Array)geometry.getAttribute("index").getArray() : null;
//		Float32Array normals = geometry.getAttribute("normal") != null
//				? (Float32Array)geometry.getAttribute("normal").getArray() : null;
//		Float32Array colors = geometry.getAttribute("color") != null
//				? (Float32Array)geometry.getAttribute("color").getArray() : null;
//		Float32Array uvs = geometry.getAttribute("uv") != null
//				? (Float32Array)geometry.getAttribute("uv").getArray() : null;
//
//		List<Vector3> tempNormals = new ArrayList<Vector3>();
//		List<Vector2> tempUVs = new ArrayList<Vector2>();
//
//		for ( int i = 0, j = 0; i < vertices.getLength(); i += 3, j += 2 ) {
//
//			this.vertices.add( new Vector3( vertices.get( i ), vertices.get( i + 1 ), vertices.get( i + 2 ) ) );
//
//			if ( normals != null ) {
//
//				tempNormals.add( new Vector3( normals.get( i ), normals.get( i + 1 ), normals.get( i + 2 ) ) );
//
//			}
//
//			if ( colors != null ) {
//
//				Color color = new Color();
//				this.colors.add( color.setRGB( colors.get( i ), colors.get( i + 1 ), colors.get( i + 2 ) ) );
//
//			}
//
//			if ( uvs != null ) {
//
//				tempUVs.add( new Vector2( uvs.get( j ), uvs.get( j + 1 ) ) );
//
//			}
//
//		}
//
//		if ( indices != null ) {
//
//			for ( int i = 0; i < indices.getLength(); i += 3 ) {
//
//				addFace( normals, colors, tempNormals, tempUVs, (int)indices.get( i ), (int)indices.get( i + 1 ), (int)indices.get( i + 2 ) );
//
//			}
//
//		} else {
//
//			for ( int i = 0; i < vertices.getLength() / 3; i += 3 ) {
//
//				addFace( normals, colors, tempNormals, tempUVs, i, i + 1, i + 2 );
//
//			}
//
//		}
//
//		this.computeFaceNormals();
//
//		if(geometry.boundingBox != null)
//			this.boundingBox = geometry.boundingBox.copy();
//
//		if(geometry.boundingSphere != null)
//			this.boundingSphere = geometry.boundingSphere.copy();
//
//		return this;
//	}

//	private void addFace(Float32Array normals, Float32Array colors, List<Vector3> tempNormals, List<Vector2> tempUVs, int a, int b, int c )
//	{
//		List<Vector3> vertexNormals = normals != null
//				? Arrays.asList( tempNormals.get( a ).copy(), tempNormals.get( b ).copy(), tempNormals.get( c ).copy() )
//				: new ArrayList<Vector3>();
//		List<Color> vertexColors = colors != null
//				? Arrays.asList( this.colors.get( a ).copy(), this.colors.get( b ).copy(), this.colors.get( c ).copy() )
//				: new ArrayList<Color>();
//
//		this.faces.add( new Face3( a, b, c, vertexNormals, vertexColors, 0 ) );
//		this.faceVertexUvs.get( 0 ).add( Arrays.asList( tempUVs.get( a ), tempUVs.get( b ), tempUVs.get( c ) ) );
//
//	}

    public Vector3 center() {

        this.computeBoundingBox();

        final Vector3 offset = new Vector3();

        offset.add(this.boundingBox.min(), this.boundingBox.max());
        offset.multiply(-0.5F);

        this.applyMatrix(new Matrix4().makeTranslation(offset.x(), offset.y(), offset.z()));
        this.computeBoundingBox();

        return offset;

    }

    /**
     * Computes face normals.
     */
    public void computeFaceNormals() {

        final Vector3 cb = new Vector3();
        final Vector3 ab = new Vector3();

        for (int f = 0, fl = this.faces.size(); f < fl; f++) {

            final Face3 face = this.faces.get(f);

            final Vector3 vA = this.vertices.get(face.getA());
            final Vector3 vB = this.vertices.get(face.getB());
            final Vector3 vC = this.vertices.get(face.getC());

            cb.sub(vC, vB);
            ab.sub(vA, vB);
            cb.cross(ab);

            cb.normalize();

            face.normal.copy(cb);

        }

    }

    public void computeVertexNormals() {
        computeVertexNormals(false);
    }

    /**
     * Computes vertex normals by averaging face normals.
     * Face normals must be existing / computed beforehand.
     */
    public void computeVertexNormals(final boolean areaWeighted) {
        final Vector3[] vertices = new Vector3[this.vertices.size()];
        Face3 face;

        for (int v = 0, vl = this.vertices.size(); v < vl; v++) {

            vertices[v] = new Vector3();

        }

        if (areaWeighted) {

            // vertex normals weighted by triangle areas
            // http://www.iquilezles.org/www/articles/normals/normals.htm

            Vector3 vA, vB, vC;
            final Vector3 cb = new Vector3();
            final Vector3 ab = new Vector3();

            for (int f = 0, fl = this.faces.size(); f < fl; f++) {

                face = this.faces.get(f);

                vA = this.vertices.get(face.a);
                vB = this.vertices.get(face.b);
                vC = this.vertices.get(face.c);

                cb.sub(vC, vB);
                ab.sub(vA, vB);
                cb.cross(ab);

                vertices[face.a].add(cb);
                vertices[face.b].add(cb);
                vertices[face.c].add(cb);

            }

        } else {

            for (int f = 0, fl = this.faces.size(); f < fl; f++) {

                face = this.faces.get(f);

                vertices[face.a].add(face.normal);
                vertices[face.b].add(face.normal);
                vertices[face.c].add(face.normal);

            }

        }

        for (int v = 0, vl = this.vertices.size(); v < vl; v++) {

            vertices[v].normalize();

        }

        for (int f = 0, fl = this.faces.size(); f < fl; f++) {

            face = this.faces.get(f);

            if (!face.getVertexNormals().isEmpty()) {
                face.getVertexNormals().set(0, vertices[face.a].copy());
                face.getVertexNormals().set(1, vertices[face.b].copy());
                face.getVertexNormals().set(2, vertices[face.c].copy());
            } else {
                face.getVertexNormals().add(0, vertices[face.a].copy());
                face.getVertexNormals().add(1, vertices[face.b].copy());
                face.getVertexNormals().add(2, vertices[face.c].copy());
            }

        }
    }

    /**
     * Computes vertex tangents.<br>
     * Based on <a href="http://www.terathon.com/code/tangent.html">terathon.com</a>
     * <p>
     * Geometry must have vertex UVs (layer 0 will be used).
     */
    @Override
    public void computeTangents() {
        Face3 face;
        Vector2[] uv = new Vector2[0];
        int v;
        final int vl;
        int f;
        int fl;
        int i;
        int vertexIndex;
        final List<Vector3> tan1 = new ArrayList<>();
        final List<Vector3> tan2 = new ArrayList<>();
        final Vector3 tmp = new Vector3();
        final Vector3 tmp2 = new Vector3();

        for (v = 0, vl = this.vertices.size(); v < vl; v++) {
            tan1.add(v, new Vector3());
            tan2.add(v, new Vector3());
        }

        for (f = 0, fl = this.faces.size(); f < fl; f++) {

            face = this.faces.get(f);
            uv = this.faceVertexUvs.get(0).get(f).toArray(uv); // use UV layer 0 for tangents

            handleTriangle(face.getA(), face.getB(), face.getC(), 0, 1, 2, uv, tan1, tan2);
        }

        for (f = 0, fl = this.faces.size(); f < fl; f++) {

            face = this.faces.get(f);

            for (i = 0; i < face.getVertexNormals().size(); i++) {

                final Vector3 n = new Vector3();
                n.copy(face.getVertexNormals().get(i));

                vertexIndex = face.getFlat()[i];

                final Vector3 t = tan1.get(vertexIndex);

                // Gram-Schmidt orthogonalize

                tmp.copy(t);
                tmp.sub(n.multiply(n.dot(t))).normalize();

                // Calculate handedness

                tmp2.crossVectors(face.getVertexNormals().get(i), t);
            }
        }

        this.setHasTangents(true);
    }

    /**
     * Compute distances between vertices for Line geometries.
     */
    public void computeLineDistances() {
        float d = 0;

        for (int i = 0, il = this.vertices.size(); i < il; i++) {
            if (i > 0) {
                d += this.vertices.get(i).distanceTo(this.vertices.get(i - 1));
            }

            this.lineDistances.add(i, d);
        }
    }

    /**
     * Computes bounding box of the geometry.
     */
    @Override
    public void computeBoundingBox() {
        if (this.boundingBox == null) {
            this.boundingBox = new Box3();
        }

        this.boundingBox.setFromPoints(this.vertices);
    }

    /**
     * Computes bounding sphere of the geometry.
     * <p>
     * Neither bounding boxes or bounding spheres are computed by default.
     * They need to be explicitly computed, otherwise they are null.
     */
    @Override
    public void computeBoundingSphere() {
        if (this.boundingSphere == null) {

            this.boundingSphere = new Sphere();

        }
        this.boundingSphere.setFromPoints(this.vertices, null);

    }

    public void merge(final Geometry geometry, final Matrix4 matrix) {

        merge(geometry, matrix, 0);

    }

    /**
     * Merge two geometries or geometry and geometry from object (using object's transform)
     *
     * @param geometry
     * @param matrix
     * @param materialIndexOffset
     */
    public void merge(final Geometry geometry, final Matrix4 matrix, final int materialIndexOffset) {

        Matrix3 normalMatrix = null;

        final int vertexOffset = this.vertices.size();
        final List<Vector3> vertices1 = this.vertices;
        final List<Vector3> vertices2 = geometry.vertices;
        final List<Face3> faces1 = this.faces;
        final List<Face3> faces2 = geometry.faces;

        final List<List<Vector2>> uvs1 = this.faceVertexUvs.get(0);
        final List<List<Vector2>> uvs2 = geometry.faceVertexUvs.get(0);

        if (matrix != null) {

            normalMatrix = new Matrix3().getNormalMatrix(matrix);

        }

        // vertices

        for (int i = 0, il = vertices2.size(); i < il; i++) {

            final Vector3 vertex = vertices2.get(i);

            final Vector3 vertexCopy = vertex.copy();

            if (matrix != null) {
                vertexCopy.apply(matrix);
            }

            vertices1.add(vertexCopy);

        }

        // faces

        for (int i = 0, il = faces2.size(); i < il; i++) {

            final Face3 face = faces2.get(i);

            final List<Vector3> faceVertexNormals = face.vertexNormals;
            final List<Color> faceVertexColors = face.vertexColors;

            final Face3 faceCopy = new Face3(face.a + vertexOffset, face.b + vertexOffset, face.c + vertexOffset);
            faceCopy.getNormal().copy(face.normal);

            if (normalMatrix != null) {

                faceCopy.getNormal().apply(normalMatrix).normalize();

            }

            for (int j = 0, jl = faceVertexNormals.size(); j < jl; j++) {

                final Vector3 normal = faceVertexNormals.get(j).copy();

                if (normalMatrix != null) {

                    normal.apply(normalMatrix).normalize();

                }

                faceCopy.vertexNormals.add(normal);

            }

            faceCopy.color.copy(face.color);

            for (int j = 0, jl = faceVertexColors.size(); j < jl; j++) {

                final Color color = faceVertexColors.get(j);
                faceCopy.vertexColors.add(color.copy());

            }

            faceCopy.materialIndex = face.materialIndex + materialIndexOffset;

            faces1.add(faceCopy);

        }

        // uvs

        for (int i = 0, il = uvs2.size(); i < il; i++) {

            final List<Vector2> uv = uvs2.get(i);
            final List<Vector2> uvCopy = new ArrayList<>();

            if (uv == null) {

                continue;

            }

            for (int j = 0, jl = uv.size(); j < jl; j++) {

                uvCopy.add(new Vector2(uv.get(j).x(), uv.get(j).y()));

            }

            uvs1.add(uvCopy);

        }

    }

    /**
     * Checks for duplicate vertices with hashmap.
     * Duplicated vertices are removed and faces' vertices are updated.
     */
    public int mergeVertices() {
        // Hashmap for looking up vertice by position coordinates (and making sure they are unique)
        final Map<String, Integer> verticesMap = new HashMap<>();
        final List<Vector3> unique = new ArrayList<>();
        final List<Integer> changes = new ArrayList<>();

        final int precisionPoints = 4; // number of decimal points, eg. 4 for epsilon of 0.0001
        final float precision = (float) Math.pow(10, precisionPoints);

        for (int i = 0, il = this.vertices.size(); i < il; i++) {
            final Vector3 v = this.vertices.get(i);
            final String key = Math.round(v.x() * precision) + "_" + Math.round(v.y() * precision) + "_" + Math.round(v.z() * precision);

            if (!verticesMap.containsKey(key)) {
                verticesMap.put(key, i);
                unique.add(v);
                changes.add(i, unique.size() - 1);
            } else {
                changes.add(i, changes.get(verticesMap.get(key)));
            }
        }


        // if faces are completely degenerate after merging vertices, we
        // have to remove them from the geometry.
        final List<Integer> faceIndicesToRemove = new ArrayList<>();

        for (int i = 0, il = this.faces.size(); i < il; i++) {
            final Face3 face = this.faces.get(i);

            face.setA(changes.get(face.getA()));
            face.setB(changes.get(face.getB()));
            face.setC(changes.get(face.getC()));

            final int[] indices = { face.getA(), face.getB(), face.getC() };

            // if any duplicate vertices are found in a Face3
            // we have to remove the face as nothing can be saved
            for (int n = 0; n < 3; n++) {
                if (indices[n] == indices[(n + 1) % 3]) {
                    faceIndicesToRemove.add(i);
                    break;
                }
            }
        }

        for (int i = faceIndicesToRemove.size() - 1; i >= 0; i--) {
            this.faces.remove(i);

            for (int j = 0, jl = this.faceVertexUvs.size(); j < jl; j++) {
                this.faceVertexUvs.get(j).remove(i);
            }
        }

        // Use unique set of vertices

        final int diff = this.vertices.size() - unique.size();
        this.vertices = unique;
        return diff;
    }

    /**
     * Creates a new copy of the Geometry.
     */
    public Geometry copy() {

        final Geometry geometry = new Geometry();


        for (int i = 0, il = this.vertices.size(); i < il; i++) {

            geometry.vertices.add(this.vertices.get(i).copy());

        }

        for (int i = 0, il = this.faces.size(); i < il; i++) {

            geometry.faces.add(this.faces.get(i).copy());
        }

        final List<List<Vector2>> uvs = this.faceVertexUvs.get(0);

        for (int i = 0, il = uvs.size(); i < il; i++) {

            final List<Vector2> uv = uvs.get(i);
            final List<Vector2> uvCopy = new ArrayList<>();

            for (int j = 0, jl = uv.size(); j < jl; j++) {

                uvCopy.add(new Vector2(uv.get(j).x(), uv.get(j).y()));

            }

            geometry.faceVertexUvs.get(0).add(uvCopy);

        }

        return geometry;

    }

    private void handleTriangle(final int a, final int b, final int c, final int ua, final int ub, final int uc, final Vector2[] uv, final List<Vector3> tan1, final List<Vector3> tan2) {
        final Vector3 vA = this.vertices.get(a);
        final Vector3 vB = this.vertices.get(b);
        final Vector3 vC = this.vertices.get(c);

        final Vector2 uvA = uv[ua];
        final Vector2 uvB = uv[ub];
        final Vector2 uvC = uv[uc];

        final float x1 = vB.x() - vA.x();
        final float x2 = vC.x() - vA.x();
        final float y1 = vB.y() - vA.y();
        final float y2 = vC.y() - vA.y();
        final float z1 = vB.z() - vA.z();
        final float z2 = vC.z() - vA.z();

        final float s1 = uvB.x() - uvA.x();
        final float s2 = uvC.x() - uvA.x();
        final float t1 = uvB.y() - uvA.y();
        final float t2 = uvC.y() - uvA.y();

        final float r = 1.0F / (s1 * t2 - s2 * t1);

        final Vector3 sdir = new Vector3();
        sdir.set((t2 * x1 - t1 * x2) * r,
            (t2 * y1 - t1 * y2) * r,
            (t2 * z1 - t1 * z2) * r);

        final Vector3 tdir = new Vector3();
        tdir.set((s1 * x2 - s2 * x1) * r,
            (s1 * y2 - s2 * y1) * r,
            (s1 * z2 - s2 * z1) * r);

        tan1.get(a).add(sdir);
        tan1.get(b).add(sdir);
        tan1.get(c).add(sdir);

        tan2.get(a).add(tdir);
        tan2.get(b).add(tdir);
        tan2.get(c).add(tdir);
    }

}