package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Vector2;

public final class Checkpoint {
    private final Vector2 p1;

    private final Vector2 p2;

    private final int index;

    private final float position;

    private final boolean required;

    public Checkpoint(final Vector2 p1, final Vector2 p2, final int index, final float position, final boolean required) {
        this.p1 = p1;
        this.p2 = p2;
        this.index = index;
        this.position = position;
        this.required = required;
    }

    public Vector2 getP1() {
        return this.p1;
    }

    public Vector2 getP2() {
        return this.p2;
    }

    public int getIndex() {
        return this.index;
    }

    public float getPosition() {
        return this.position;
    }

    public boolean isRequired() {
        return this.required;
    }

    @Override
    public String toString() {
        return "CheckPoint{" +
            "p1=" + this.p1 +
            ", p2=" + this.p2 +
            ", index=" + this.index +
            ", position=" + this.position +
            ", required=" + this.required +
            '}';
    }
}
