package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Vector2;

public interface PathLocator {
    Vector2 locate(final Mapper map);
}
