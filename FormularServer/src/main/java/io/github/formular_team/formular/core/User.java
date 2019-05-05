package io.github.formular_team.formular.core;

import java.util.UUID;

import io.github.formular_team.formular.core.color.Color;

public final class User {
    private final UUID uuid;

    private final String name;

    private final Color color;

    private User(final UUID uuid, final String name, final Color color) {
        this.uuid = uuid;
        this.name = name;
        this.color = color;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return "User{" +
            "uuid=" + this.uuid +
            ", name='" + this.name + '\'' +
            ", color=" + this.color  +
            '}';
    }

    public static User create(final UUID uuid, final String name, final Color color) {
        return new User(uuid, name, color);
    }
}
