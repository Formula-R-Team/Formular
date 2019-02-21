package io.github.formular_team.formular.server;

import java.time.Instant;

import io.github.formular_team.formular.User;

public interface CourseMetadata {
    User creator();

    Instant creationDate();

    String name();
}
