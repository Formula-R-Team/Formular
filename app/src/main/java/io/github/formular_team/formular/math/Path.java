package io.github.formular_team.formular.math;

public class Path implements Curve {
    Path(final Builder builder) {

    }

    @Override
    public Vector2 getPoint(final float t) {
        return null;
    }

    @Override
    public Vector2 getPointAt(final float u) {
        return null;
    }

    @Override
    public Vector2[] getPoints(final int divisions) {
        return new Vector2[0];
    }

    @Override
    public Vector2[] getSpacedPoints(final int divisions) {
        return new Vector2[0];
    }

    @Override
    public float getLength() {
        return 0;
    }

    @Override
    public float[] getLengths(final int divisions) {
        return new float[0];
    }

    @Override
    public Vector2 getTangent(final float t) {
        return null;
    }

    @Override
    public Vector2 getTangentAt(final float u) {
        return null;
    }

    @Override
    public Path copy() {
        return null;
    }

    public void visit(final Builder visitor) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Builder() {}

        public Builder moveTo(final Vector2 point) {
            return this;
        }

        public Builder lineTo(final Vector2 point) {
            return this;
        }

        public Builder bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
            return this;
        }

        public Builder closed(final boolean closed) {
            return this;
        }

        public Path build() {
            return new Path(this);
        }
    }
}
