package io.github.formular_team.formular.core.course.track;

import io.github.formular_team.formular.core.math.curve.Path;

public interface PathSimplifier {
    Path simplify(final Path path);
}
