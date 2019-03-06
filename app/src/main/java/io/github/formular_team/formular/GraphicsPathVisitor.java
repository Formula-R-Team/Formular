package io.github.formular_team.formular;

import android.graphics.Path;

import io.github.formular_team.formular.math.PathVisitor;
import io.github.formular_team.formular.math.Vector2;

public final class GraphicsPathVisitor implements PathVisitor {
    private final Path path;

    public GraphicsPathVisitor(final Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public PathVisitor moveTo(final Vector2 point) {
        this.path.moveTo(point.x(), point.y());
        return this;
    }

    @Override
    public PathVisitor lineTo(final Vector2 point) {
        this.path.lineTo(point.x(), point.y());
        return this;
    }

    @Override
    public PathVisitor bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
        this.path.cubicTo(controlA.x(), controlA.y(), controlB.x(), controlB.y(), point.x(), point.y());
        return this;
    }

    @Override
    public PathVisitor closePath() {
        this.path.close();
        return this;
    }
}
