package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Vector2;

public final class CheckPoint {
    private final Vector2 p1;

    private final Vector2 p2;

    private final int index;

    private final float start;

    private final float end;

    private final boolean required;

    public CheckPoint(final Vector2 p1, final Vector2 p2, final int index, final float start, final float end, final boolean required) {
        this.p1 = p1;
        this.p2 = p2;
        this.index = index;
        this.start = start;
        this.end = end;
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

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public boolean isRequired() {
        return this.required;
    }
}
