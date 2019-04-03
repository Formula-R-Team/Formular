package io.github.formular_team.formular;

import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.trace.Mapper;

public interface PathLocator {
    Vector2 locate(final Mapper map);
}
