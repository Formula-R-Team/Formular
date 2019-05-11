package io.github.formular_team.formular.core.course;

import io.github.formular_team.formular.core.math.curve.Path;

public interface CourseFactory {
    Course create(final Path path, final CourseMetadata metadata);
}
