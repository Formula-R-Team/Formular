package io.github.formular_team.formular.math;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

public class Shape extends Path {
    private Shape(final Builder builder) {
        super(builder);
    }

    @Override
    public Shape copy() {
        final Builder newBuilder = new Builder();
        super.copy(newBuilder);
        return newBuilder.build();
    }

    public Path[] holes() {
        return new Path[0];
    }

    public List<List<Vector2>> getPointsHoles(final int divisions) {
        return Lists.newArrayList();
    }

    public List<List<Vector2>> getPointsHoles() {
        return this.getPointsHoles(200);
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
