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
    public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
        this.path.cubicTo(aCP1x, aCP1y, aCP2x, aCP2y, aX, aY);
    }

    @Override
    public void closePath() {
        this.path.close();
    }
}
