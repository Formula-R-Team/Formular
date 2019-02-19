package io.github.formular_team.formular.math;

public interface Shape extends Path {
    @Override
    Shape copy();

    Path holes();

    Vector2[] getPointsHoles(final int divisions);

    boolean contains(final Vector2 point);
}
