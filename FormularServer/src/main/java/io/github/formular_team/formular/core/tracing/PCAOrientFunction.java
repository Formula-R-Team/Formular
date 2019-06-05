package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Mth;

/**
 * An implementation of an orient function which performs a principle component analysis to detect orientation.
 */
public final class PCAOrientFunction implements OrientFunction {
	private final int radius;

	private final float[] bufX;

	private final float[] bufY;

	private final float[] bufW;

	/**
	 * Constructs a {@link PCAOrientFunction} with the specified radius
	 *
	 * @param radius radius to determine orientation within
	 */
	public PCAOrientFunction(final int radius) {
		this.radius = radius;
		final int count = (2 * this.radius + 1) * (2 * this.radius + 1);
		this.bufX = new float[count];
		this.bufY = new float[count];
		this.bufW = new float[count];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getOrientation(final Mapper image) {
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
		return Mth.PI * 0.5F - PCA.get(this.bufX, this.bufY, this.bufW, n).getAngle();
	}
}
