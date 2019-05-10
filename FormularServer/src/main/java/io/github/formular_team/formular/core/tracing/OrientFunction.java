package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Mth;

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
		if (n == 0) {
			return Float.NaN;
		}
		final float a = cov(this.bufX, this.bufX, this.bufW, n);
		final float b = cov(this.bufX, this.bufY, this.bufW, n);
		final float d = cov(this.bufY, this.bufY, this.bufW, n);
        return this.computeSpreadAngle(d, b, a);
	}

	private float computeSpreadAngle(final float a, final float b, final float d) {
		return Mth.atan2(b == 0.0F ? 0.0F : (Mth.sqrt((a - d) * (a - d) + 4.0F * b * b) + a - d) / (2.0F * b), 1.0F);
	}

	// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online
	private static float cov(final float[] data1, final float[] data2, final float[] data3, final int n) {
		double meanx = 0.0D, meany = 0.0D;
		double wsum = 0.0D, wsum2 = 0.0D;
		double C = 0.0D;
		for (int i = 0; i < n; i++) {
			final double x = data1[i];
			final double y = data2[i];
			final double w = data3[i];
			wsum += w;
			wsum2 += w * w;
			final double dx = x - meanx;
			meanx += (w / wsum) * dx;
			meany += (w / wsum) * (y - meany);
			C += w * dx * (y - meany);
		}
		return (float) (C / (wsum - wsum2 / wsum));
	}
}
