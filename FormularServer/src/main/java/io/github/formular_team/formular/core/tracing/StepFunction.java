package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Vector2;

public interface StepFunction {
	float getSize();

	Vector2 step(final Mapper image);
}
