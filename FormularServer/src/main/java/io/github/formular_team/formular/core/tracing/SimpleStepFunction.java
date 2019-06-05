package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Vector2;

/**
 * An implementation of {@link StepFunction} that creates evenly spaced steps of a defined view distance within a specified field of view.
 */
public final class SimpleStepFunction implements StepFunction {
	private final float viewDistance;

	private final float viewHalfAngle;

	private final int viewHalfLength;

	/**
	 * Constructs a {@link SimpleStepFunction} with the specified view distance and field of view.
	 *
	 * @param viewDistance view distance to look ahead for next step
	 * @param fieldOfView field of view in radians to find next step
	 */
	public SimpleStepFunction(final float viewDistance, final float fieldOfView) {
		this.viewDistance = viewDistance;
		this.viewHalfAngle = 0.5F * fieldOfView;
		this.viewHalfLength = (int) Math.ceil(this.viewDistance * this.viewHalfAngle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getSize() {
		return this.viewDistance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector2 getStep(final Mapper image) {
		final Vector2 result = new Vector2();
		float greatestStrength = Float.NEGATIVE_INFINITY;
		for (int n = -this.viewHalfLength; n <= this.viewHalfLength; n++) {
			final Vector2 vec = new Vector2(1.0F, 0.0F).rotateAround(new Vector2(), n * this.viewHalfAngle / this.viewHalfLength);
			vec.multiply(this.viewDistance);
			final float strength = image.get(vec.getX(), vec.getY());
			if (strength > 0.15F && strength > greatestStrength) {
				greatestStrength = strength;
				result.copy(vec);
			}
		}
		return result;
	}
}
