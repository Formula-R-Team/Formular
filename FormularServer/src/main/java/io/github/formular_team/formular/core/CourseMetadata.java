package io.github.formular_team.formular.core;

public final class CourseMetadata {
    private final User creator;

    private final long creationDate;

    private final String name;

    private CourseMetadata(final User creator, final long creationDate, final String name) {
        this.creator = creator;
        this.creationDate = creationDate;
        this.name = name;
    }

    public User creator() {
        return this.creator;
    }

    public long creationDate() {
        return this.creationDate;
    }

    public String name() {
        return this.name;
    }

    public static CourseMetadata create(final User creator, final long creationDate, final String name) {
        return new CourseMetadata(creator, creationDate, name);
    }
}
