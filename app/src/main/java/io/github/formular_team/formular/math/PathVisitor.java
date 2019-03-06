package io.github.formular_team.formular.math;

import java.util.function.Function;

public interface PathVisitor {
    PathVisitor moveTo(final Vector2 point);

    PathVisitor lineTo(final Vector2 point);

    PathVisitor bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point);

    PathVisitor closePath();

    default PathVisitor transform(final Function<Vector2, Vector2> function) {
        return new PathVisitor() {
            @Override
            public PathVisitor moveTo(final Vector2 point) {
                return PathVisitor.this.moveTo(this.transform(point));
            }

            @Override
            public PathVisitor lineTo(final Vector2 point) {
                return PathVisitor.this.lineTo(this.transform(point));
            }

            @Override
            public PathVisitor bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
                return PathVisitor.this.bezierCurveTo(this.transform(controlA), this.transform(controlB), this.transform(point));
            }

            @Override
            public PathVisitor closePath() {
                return PathVisitor.this.closePath();
            }

            private Vector2 transform(final Vector2 vector) {
                return function.apply(vector);
            }
        };
    }
}
