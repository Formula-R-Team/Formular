package io.github.formular_team.formular.tracer;

public final class SimpleStepFunction implements StepFunction {
	private final double viewDistance;

	private final double viewHalfAngle;

	private final int viewHalfLength;

	public SimpleStepFunction(final double viewDistance, final double fieldOfView) {
		this.viewDistance = viewDistance;
		this.viewHalfAngle = 0.5D * fieldOfView;
		this.viewHalfLength = (int) Math.ceil(this.viewDistance * this.viewHalfAngle);
	}

	@Override
	public double getSize() {
		return this.viewDistance;
	}

	@Override
	public Vec2 step(final Mapper image) {
		final Vec2 result = new Vec2(0.0D, 0.0D);
		double greatestStrength = Double.NEGATIVE_INFINITY;
		for (int n = -this.viewHalfLength; n <= this.viewHalfLength; n++) {
			final Vec2 vec = new Vec2(n * this.viewHalfAngle / this.viewHalfLength);
			vec.mul(this.viewDistance);
			final double strength = image.get(vec.getX(), vec.getY());
			if (strength > 0.15D && strength > greatestStrength) {
				greatestStrength = strength;
				result.set(vec);
			}
		}
		return result;
	}
}
