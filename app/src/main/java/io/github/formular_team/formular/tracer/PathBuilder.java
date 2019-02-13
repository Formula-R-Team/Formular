package io.github.formular_team.formular.tracer;

public interface PathBuilder {
    void moveTo(final double x, final double y);

    void lineTo(final double x, final double y);

    void closePath();
}
