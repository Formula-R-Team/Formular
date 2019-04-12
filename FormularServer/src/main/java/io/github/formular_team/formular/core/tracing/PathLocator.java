package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Vector2;

public interface PathLocator {
    Vector2 locate(final Mapper map);
}
