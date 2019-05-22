package io.github.formular_team.formular.core.tracing;

public final class OrientFunction implements ToFloatMapFunction {
	private final int radius;

	private final float[] bufX;

	private final float[] bufY;

	private final float[] bufW;

	public OrientFunction(final int radius) {
		this.radius = radius;
		final int count = (2 * this.radius + 1) * (2 * this.radius + 1);
		this.bufX = new float[count];
		this.bufY = new float[count];
		this.bufW = new float[count];
	}

	@Override
	public float orient(final Mapper image) {
		int n = 0;
		for (int x = -this.radius; x <= this.radius; x++) {
			for (int y = -this.radius; y <= this.radius; y++) {
				final float w = image.get(x, y);
				if (w != 0.0F) {
					this.bufX[n] = x;
					this.bufY[n] = y;
					this.bufW[n] = w;
					n++;
				}
			}
		}
		return PCA.get(this.bufX, this.bufY, this.bufW, n).getAngle();
	}
}
