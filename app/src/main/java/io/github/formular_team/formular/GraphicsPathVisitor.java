package io.github.formular_team.formular;

import android.graphics.Path;

import io.github.formular_team.formular.core.math.PathVisitor;

public final class GraphicsPathVisitor implements PathVisitor {
    private final Path path;

    public GraphicsPathVisitor(final Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public void moveTo(final float x, final float y) {
        this.path.moveTo(x, y);
    }

    @Override
    public void lineTo(final float x, final float y) {
        this.path.lineTo(x, y);
    }

    @Override
    public void bezierCurveTo(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
        this.path.cubicTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void closePath() {
        this.path.close();
    }
}
