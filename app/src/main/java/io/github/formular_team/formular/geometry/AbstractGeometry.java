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

import io.github.formular_team.formular.math.Box3;
import io.github.formular_team.formular.math.Sphere;

public abstract class AbstractGeometry {
    private static int nextId = 0;

    private int id;

    private String name;

    protected Box3 boundingBox = null;

    protected Sphere boundingSphere = null;

    protected boolean verticesNeedUpdate = false;

    protected boolean elementsNeedUpdate = false;

    protected boolean normalsNeedUpdate = false;

    protected boolean colorsNeedUpdate = false;

    protected boolean uvsNeedUpdate = false;

    protected boolean tangentsNeedUpdate = false;

    protected boolean morphTargetsNeedUpdate = false;

    protected boolean lineDistancesNeedUpdate = false;

    protected boolean groupsNeedUpdate = false;

    public AbstractGeometry() {
        this.id = nextId++;
        this.name = "";
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public boolean isVerticesNeedUpdate() {
        return this.verticesNeedUpdate;
    }

    public void setVerticesNeedUpdate(final boolean verticesNeedUpdate) {
        this.verticesNeedUpdate = verticesNeedUpdate;
    }

    public boolean isElementsNeedUpdate() {
        return this.elementsNeedUpdate;
    }

    public void setElementsNeedUpdate(final boolean elementsNeedUpdate) {
        this.elementsNeedUpdate = elementsNeedUpdate;
    }

    public boolean isNormalsNeedUpdate() {
        return this.normalsNeedUpdate;
    }

    public void setNormalsNeedUpdate(final boolean normalsNeedUpdate) {
        this.normalsNeedUpdate = normalsNeedUpdate;
    }

    public boolean isColorsNeedUpdate() {
        return this.colorsNeedUpdate;
    }

    public void setColorsNeedUpdate(final boolean colorsNeedUpdate) {
        this.colorsNeedUpdate = colorsNeedUpdate;
    }

    public boolean isUvsNeedUpdate() {
        return this.uvsNeedUpdate;
    }

    public void setUvsNeedUpdate(final boolean uvsNeedUpdate) {
        this.uvsNeedUpdate = uvsNeedUpdate;
    }

    public boolean isTangentsNeedUpdate() {
        return this.tangentsNeedUpdate;
    }

    public void setTangentsNeedUpdate(final boolean tangentsNeedUpdate) {
        this.tangentsNeedUpdate = tangentsNeedUpdate;
    }

    public boolean isLineDistancesNeedUpdate() {
        return this.lineDistancesNeedUpdate;
    }

    public void setLineDistancesNeedUpdate(final boolean lineDistancesNeedUpdate) {
        this.lineDistancesNeedUpdate = lineDistancesNeedUpdate;
    }

    public boolean isGroupsNeedUpdate() {
        return this.groupsNeedUpdate;
    }

    public void setGroupsNeedUpdate(final boolean groupsNeedUpdate) {
        this.groupsNeedUpdate = groupsNeedUpdate;
    }

    public Box3 getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(final Box3 boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Sphere getBoundingSphere() {
        return this.boundingSphere;
    }

    public void setBoundingSphere(final Sphere boundingSphere) {
        this.boundingSphere = boundingSphere;
    }

    public abstract void computeBoundingBox();

    public abstract void computeBoundingSphere();

    public abstract void computeVertexNormals();

    public abstract void computeTangents();
}
