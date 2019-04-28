package io.github.formular_team.formular.core;

public final class User {
    private final String name;

    private final int color;

    private User(final String name, final int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + this.name + '\'' +
            ", color=" + Long.toHexString(0x100000000L | (long) this.color).substring(1) +
            '}';
    }

    public static User create(final String name, final int color) {
        return new User(name, color);
    }
}
