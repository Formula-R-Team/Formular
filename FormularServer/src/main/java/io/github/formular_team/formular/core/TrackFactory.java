package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Path;

public interface TrackFactory {
    Track create(final Path path);
}
