package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Vector2;

public final class Pose {
    private final Vector2 position;

    private final float rotation;

    public Pose(final Vector2 position, final float rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public float getRotation() {
        return this.rotation;
    }
}
