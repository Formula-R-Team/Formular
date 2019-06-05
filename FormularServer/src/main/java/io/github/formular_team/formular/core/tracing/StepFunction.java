package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Vector2;

/**
 * An object which locates the next point along the current path to advance to.
 */
public interface StepFunction {
	/**
	 * Returns the length of steps produced by this object
	 *
	 * @return size of step
	 */
	float getSize();

	/**
	 * Applies this function to the given image.
	 *
	 * @param image image to determine step
	 * @return relative step vector
	 */
	Vector2 getStep(final Mapper image);
}
