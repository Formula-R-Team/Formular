package io.github.formular_team.formular.server;

import java.time.Instant;

import io.github.formular_team.formular.User;

public final class CourseMetadata {
    private final User creator;

    private final Instant creationDate;

    private final String name;

    private CourseMetadata(final User creator, final Instant creationDate, final String name) {
        this.creator = creator;
        this.creationDate = creationDate;
        this.name = name;
    }

    public User creator() {
        return this.creator;
    }

    public Instant creationDate() {
        return this.creationDate;
    }

    public String name() {
        return this.name;
    }

    public CourseMetadata create(final User creator, final Instant creationDate, final String name) {
        return new CourseMetadata(creator, creationDate, name);
    }
}
