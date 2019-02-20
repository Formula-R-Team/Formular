package io.github.formular_team.formular.server;

import com.google.common.collect.ImmutableList;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.Vector2;

public interface Track {
    Path roadPath();

    Shape roadShape();

    ImmutableList<CheckPoint> checkPoints();

    interface CheckPoint {
        Vector2 p1();

        Vector2 p2();
    }
}
