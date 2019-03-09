package io.github.formular_team.formular.math;

public interface PathVisitor {
    void moveTo(final float x, final float y);

    void lineTo(final float x, final float y);

    void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY);

    void closePath();
}
