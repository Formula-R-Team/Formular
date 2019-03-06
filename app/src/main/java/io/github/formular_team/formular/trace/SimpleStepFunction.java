package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Vector2;

public final class SimpleStepFunction implements StepFunction {
	private final float viewDistance;

	private final float viewHalfAngle;

	private final int viewHalfLength;

	public SimpleStepFunction(final float viewDistance, final float fieldOfView) {
		this.viewDistance = viewDistance;
		this.viewHalfAngle = 0.5F * fieldOfView;
		this.viewHalfLength = (int) Math.ceil(this.viewDistance * this.viewHalfAngle);
	}

	@Override
	public float getSize() {
		return this.viewDistance;
	}

	@Override
	public Vector2 step(final Mapper image) {
		final Vector2 result = new Vector2();
		float greatestStrength = Float.NEGATIVE_INFINITY;
		for (int n = -this.viewHalfLength; n <= this.viewHalfLength; n++) {
			final Vector2 vec = new Vector2(1.0F, 0.0F).rotateAround(new Vector2(), n * this.viewHalfAngle / this.viewHalfLength);
			vec.multiply(this.viewDistance);
			final float strength = image.get(vec.x(), vec.y());
			if (strength > 0.15F && strength > greatestStrength) {
				greatestStrength = strength;
				result.copy(vec);
			}
		}
		return result;
	}
}
