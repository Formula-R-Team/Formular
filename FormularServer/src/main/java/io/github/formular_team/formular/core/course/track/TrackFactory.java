package io.github.formular_team.formular.core.course.track;

import io.github.formular_team.formular.core.math.curve.Path;

public interface TrackFactory {
    Track create(final Path path);
}
