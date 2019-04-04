package io.github.formular_team.formular;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.server.Track;

public interface TrackFactory {
    Track create(final Path path);
}
