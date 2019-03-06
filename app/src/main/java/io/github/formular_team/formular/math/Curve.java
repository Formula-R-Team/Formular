package io.github.formular_team.formular.math;

public interface Curve {
    Vector2 getPoint(final float t);

    Vector2 getPointAt(final float u);

    Vector2[] getPoints(final int divisions);

    Vector2[] getPoints();

    Vector2[] getSpacedPoints(final int divisions);

    Vector2[] getSpacedPoints();

    float getLength();

    float[] getLengths(final int divisions);

    float[] getLengths();

    Vector2 getTangent(final float t);

    Vector2 getTangentAt(final float u);

    Curve copy();
}
