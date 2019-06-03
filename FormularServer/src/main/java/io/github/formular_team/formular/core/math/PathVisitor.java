package io.github.formular_team.formular.core.math;

/**
 * An object which can receive a sequence instructions used to build a path.
 */
public interface PathVisitor {
    /**
     * Adds a point to the path by moving to the specified coordinates.
     *
     * @param x x coordinate of the point to move to
     * @param y x coordinate of the point to move to
     */
    void moveTo(final float x, final float y);

    /**
     * Adds a point to the path by forming a line segment from the current coordinates to the specified coordinates.
     *
     * @param x x coordinate of the point to form segment to
     * @param y y coordinate of the point to form segment to
     */
    void lineTo(final float x, final float y);

    /**
     * Adds a curved segmented to the path by forming a B&eacute;zier curve with end points at current coordinates and the specified coordinates {@code (x3,y3)}, using the specified points {@code (x1,y1)} and {@code (x2,y2)} as B&eacute;zier control points.
     *
     * @param x1 x coordinate of the first B&eacute;zier control point
     * @param y1 y coordinate of the first B&eacute;zier control point
     * @param x2 x coordinate of the second B&eacute;zier control point
     * @param y2 y coordinate of the second B&eacute;zier control point
     * @param x3 x coordinate of the final end point
     * @param y3 y coordinate of the final end point
     */
    void bezierCurveTo(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3);

    /**
     * Closes this path forming a line segment from the current coordinates to the start coordinates.
     */
    void closePath();
}
