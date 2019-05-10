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

package io.github.formular_team.formular.core.geom;

import java.util.ArrayList;
import java.util.List;

import io.github.formular_team.formular.core.math.Curve;
import io.github.formular_team.formular.core.math.Matrix4;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Vector3;

public class FrenetFrames {
    private static final float EPSILON = 0.0001F;

    private final List<Vector3> tangents;

    private final List<Vector3> normals;

    private final List<Vector3> binormals;

    private final Curve path;

    public FrenetFrames(final Curve path, final int segments, final boolean closed) {
        this.path = path;

        this.tangents = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.binormals = new ArrayList<>();

        final Matrix4 mat = new Matrix4();

        // compute the tangent vectors for each segment on the path
        for (int i = 0; i <= segments; i++) {
            final float u = i / (float) segments;
            final Vector3 vec = (Vector3) path.getTangentAt(u);
            this.tangents.add(vec.normalize());
        }

        this.initialNormal3();

        final Vector3 vec = new Vector3();

        // compute the slowly-varying normal and binormal vectors for each segment on the path
        for (int i = 1; i <= segments; i++) {
            this.normals.add(this.normals.get(i - 1).copy());
            this.binormals.add(this.binormals.get(i - 1).copy());
            vec.cross(this.tangents.get(i - 1), this.tangents.get(i));
            if (vec.length() > EPSILON) {
                vec.normalize();
                final float aCos = this.tangents.get(i - 1).dot(this.tangents.get(i));
                final float theta = Mth.acos(Math.min(1.0F, aCos));
                this.normals.get(i).apply(mat.makeRotationAxis(vec, theta));
            }
            this.binormals.get(i).cross(this.tangents.get(i), this.normals.get(i));

        }

        // if the curve is closed, post-process the vectors so the first and last normal vectors are the same
        if (closed) {
            float theta = Mth.acos(this.normals.get(0).dot(this.normals.get(segments))) / segments;
            if (theta > EPSILON) {
                if (this.tangents.get(0).dot(vec.cross(this.normals.get(0), this.normals.get(segments))) > 0) {
                    theta = -theta;
                }
                for (int i = 1; i <= segments; i++) {
                    this.normals.get(i).apply(mat.makeRotationAxis(this.tangents.get(i), theta * i));
                    this.binormals.get(i).cross(this.tangents.get(i), this.normals.get(i));
                }
            }
        }
    }

    public List<Vector3> getTangents() {
        return this.tangents;
    }

    public List<Vector3> getNormals() {
        return this.normals;
    }

    public List<Vector3> getBinormals() {
        return this.binormals;
    }

    private void initialNormal1() {
        this.initialNormal1(new Vector3(0.0F, 0.0F, 1.0F));
    }

    private void initialNormal1(final Vector3 lastBinormal) {
        // fixed start binormal. Has dangers of 0 vectors
        this.normals.add(new Vector3());
        this.binormals.add(new Vector3());

        this.normals.get(0).cross(lastBinormal, this.tangents.get(0)).normalize();
        this.binormals.get(0).cross(this.tangents.get(0), this.normals.get(0)).normalize();
    }

    private void initialNormal2() {
        // This uses the Frenet-Serret formula for deriving binormal
        final Vector3 t2 = (Vector3) this.path.getTangentAt(EPSILON);

        this.normals.add(new Vector3().sub(t2, this.tangents.get(0)).normalize());
        this.binormals.add(new Vector3().cross(this.tangents.get(0), this.normals.get(0)));

        this.normals.get(0).cross(this.binormals.get(0), this.tangents.get(0)).normalize(); // last binormal x tangent
        this.binormals.get(0).cross(this.tangents.get(0), this.normals.get(0)).normalize();

    }

    /*
     * select an initial normal vector perpendicular to the first tangent vector,
     * and in the direction of the smallest tangent xyz component
     */
    private void initialNormal3() {
        this.normals.add(0, new Vector3());
        this.binormals.add(0, new Vector3());
        float smallest = Float.MAX_VALUE;

        final float tx = Math.abs(this.tangents.get(0).getX());
        final float ty = Math.abs(this.tangents.get(0).getY());
        final float tz = Math.abs(this.tangents.get(0).getZ());

        final Vector3 normal = new Vector3();
        if (tx <= smallest) {
            smallest = tx;
            normal.set(1.0F, 0.0F, 0.0F);
        }

        if (ty <= smallest) {
            smallest = ty;
            normal.set(0.0F, 1.0F, 0.0F);
        }

        if (tz <= smallest) {
            normal.set(0.0F, 0.0F, 1.0F);
        }

        final Vector3 vec = new Vector3();
        vec.cross(this.tangents.get(0), normal).normalize();

        this.normals.get(0).cross(this.tangents.get(0), vec);
        this.binormals.get(0).cross(this.tangents.get(0), this.normals.get(0));
    }
}