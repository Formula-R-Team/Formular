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
package io.github.formular_team.formular.core.math.curve;

import java.util.ArrayList;
import java.util.List;

import io.github.formular_team.formular.core.geom.ExtrudeGeometry;
import io.github.formular_team.formular.core.math.Vector2;

public class Shape extends Path {
    private final List<Path> holes = new ArrayList<>();

    public Shape() {
        super();
    }

    public Shape(final List<Vector2> points) {
        super(points);
    }

    public List<Path> getHoles() {
        return this.holes;
    }

    public ExtrudeGeometry extrude(final ExtrudeGeometry.ExtrudeGeometryParameters options) {
        return new ExtrudeGeometry(this, options);
    }

//	public ShapeGeometry makeGeometry ( final ShapeGeometry.ShapeGeometryParameters options )
//	{
//		return new ShapeGeometry( this, options );
//	}

    public List<List<Vector2>> getPointsHoles() {
        return this.getPointsHoles(false);
    }

    public List<List<Vector2>> getPointsHoles(final boolean closedPath) {
        final int il = this.holes.size();
        final List<List<Vector2>> holesPts = new ArrayList<>();
		for (int i = 0; i < il; i++) {
			holesPts.add(this.holes.get(i).getPoints(closedPath)/*.getTransformedPoints(closedPath, this.getBends())*/);
		}
        return holesPts;
    }

    public List<List<Vector2>> getSpacedPointsHoles(final boolean closedPath) {
        final int il = this.holes.size();
        final List<List<Vector2>> holesPts = new ArrayList<>();
		for (int i = 0; i < il; i++) {
			holesPts.add(this.holes.get(i).getPoints(closedPath)/*.getTransformedSpacedPoints(closedPath, this.getBends())*/);
		}
        return holesPts;
    }

    @Override
    public Shape clone() {
        return new Shape().copy(this);
    }

    @Override
    public Shape copy(final Path other) {
        super.copy(other);
        return this;
    }

    public Shape copy(final Shape other) {
        super.copy(other);
        this.holes.clear();
        for (final Path hole : other.holes) {
            this.holes.add(hole.clone());
        }
        return this;
    }
}