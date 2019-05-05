package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Bezier;
import io.github.formular_team.formular.core.math.Path;

public final class BezierPathSimplifier implements PathSimplifier {
    private final float error;

    private BezierPathSimplifier(final float error) {
        this.error = error;
    }

    @Override
    public Path simplify(final Path path) {
        return Bezier.fitCurve(path, this.error);
    }

    public static BezierPathSimplifier create(final float error) {
        return new BezierPathSimplifier(error);
    }
}
