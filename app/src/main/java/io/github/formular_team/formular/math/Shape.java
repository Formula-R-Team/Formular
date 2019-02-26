package io.github.formular_team.formular.math;

import java.util.Random;

public class Shape extends Path {
    private Shape(final Builder builder) {
        super(builder);
    }

    @Override
    public Shape copy() {
        return null;
    }

    public Path[] holes() {
        return null;
    }

    public Vector2[] getPointsHoles(final int divisions) {
        return null;
    }

    public boolean contains(final Vector2 point) {
        return false;
    }

    public Vector2 getRandomPoint(final Random rng) {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Path.Builder {
        Builder() {}

        public Builder addHole(final Path hole) {
            return this;
        }

        @Override
        public Shape build() {
            return new Shape(this);
        }
    }
}
