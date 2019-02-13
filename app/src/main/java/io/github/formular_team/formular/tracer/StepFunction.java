package io.github.formular_team.formular.tracer;

public interface StepFunction {
    double getSize();

    Vec2 step(final Mapper image);
}
