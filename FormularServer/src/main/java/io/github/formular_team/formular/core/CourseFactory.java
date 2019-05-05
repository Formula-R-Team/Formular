package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Path;

public interface CourseFactory {
    Course create(final Path path, final CourseMetadata metadata);
}
