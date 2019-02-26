package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Matrix3;

public interface SceneryItem {
    NamespacedString type();

    Matrix3 transform();
}
