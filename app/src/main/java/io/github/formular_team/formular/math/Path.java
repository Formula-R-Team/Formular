package io.github.formular_team.formular.math;

public interface Path extends Curve {
    @Override
    Path copy();

    void visit(final Builder visitor);

    interface Builder {
        Builder moveTo(final Vector2 point);

        Builder lineTo(final Vector2 point);

        Builder bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point);

        Builder closed(boolean closed);

        Path build();
    }
}
