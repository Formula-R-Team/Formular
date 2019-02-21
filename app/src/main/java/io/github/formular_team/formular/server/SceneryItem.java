package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Vector2;

public interface SceneryItem {
    NamespacedString type();

    Vector2 position();

    float rotationY();
}
