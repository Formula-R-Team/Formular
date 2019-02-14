package io.github.formular_team.formular.engine;

import java.time.Instant;

import io.github.formular_team.formular.User;

public interface CourseMetadata {
    User creator();

    Instant creationDate();

    String name();
}
