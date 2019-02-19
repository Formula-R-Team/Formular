package io.github.formular_team.formular.engine;

import java.util.Vector;

public interface SceneryItem {
    NamespacedString type();

    Vector position();

    float rotationY();
}
