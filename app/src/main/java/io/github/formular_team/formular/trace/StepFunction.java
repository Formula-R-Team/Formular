package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Vector2;

public interface StepFunction {
	float getSize();

	Vector2 step(final Mapper image);
}
