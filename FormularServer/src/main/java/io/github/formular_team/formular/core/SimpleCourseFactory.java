package io.github.formular_team.formular.core;

import java.util.Objects;

import io.github.formular_team.formular.core.math.Path;

public final class SimpleCourseFactory implements CourseFactory {
    private final TrackFactory trackFactory;

    public SimpleCourseFactory(final TrackFactory trackFactory) {
        this.trackFactory = Objects.requireNonNull(trackFactory);
    }

    @Override
    public Course create(final Path path, final CourseMetadata metadata) {
        return Course.builder()
            .setMetadata(metadata)
            .setTrack(this.trackFactory.create(path))
            .build();
    }
}
