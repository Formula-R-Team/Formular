package io.github.formular_team.formular.math;

import com.google.common.collect.ImmutableList;

public final class PathStroker {
    public static final class Frame {
        private final float t;

        private final Vector2 left;

        private final Vector2 right;

        private Frame(final float t, final Vector2 left, final Vector2 right) {
            this.t = t;
            this.left = left;
            this.right = right;
        }

        public float getT() {
            return this.t;
        }

        public Vector2 getLeft() {
            return this.left;
        }

        public Vector2 getRight() {
            return this.right;
        }
    }

    public static ImmutableList<Frame> create(final Curve path, final int steps, final float width) {
        final int count = path.isClosed() ? steps : steps + 1;
        final ImmutableList.Builder<Frame> frames = ImmutableList.builderWithExpectedSize(count);
        final Vector2 zero = new Vector2();
        for (int n = 0; n < count; n++) {
            final float t = n / (float) count;
            final Vector2 point = path.getPointAt(t);
            final Vector2 normal = path.getTangentAt(t).rotateAround(zero, Mth.PI / 2.0F);
            // TODO: self intersection
            frames.add(new Frame(
                t,
                normal.clone().multiply(-0.5F * width).add(point),
                normal.clone().multiply(0.5F * width).add(point)
            ));
        }
        return frames.build();
    }
}
