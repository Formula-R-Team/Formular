package io.github.formular_team.formular.tracer;

public final class BilinearMapper implements Mapper {
	private final ImageBuffer image;

	public BilinearMapper(final ImageBuffer image) {
		this.image = image;
	}

	@Override
	public double get(final double x, final double y) {
		final int ix = (int) x;
		final int iy = (int) y;
		final double fx = x - ix;
		final double fy = y - iy;
		final double v00 = this.image.get(ix, iy);
		if (fx == 0.0D && fy == 0.0D) {
			return v00;
		}
		final double v10 = this.image.get(ix + 1, iy);
		final double v01 = this.image.get(ix, iy + 1);
		final double v11 = this.image.get(ix + 1, iy + 1);
		return this.blerp(v00, v10, v01, v11, fx, fy);
	}

	private double blerp(final double v00, final double v10, final double v01, final double v11, final double tx, final double ty) {
		return this.lerp(this.lerp(v00, v10, tx), this.lerp(v01, v11, tx), ty);
	}

	private double lerp(final double v0, final double v1, final double t) {
		return (1.0D - t) * v0 + t * v1;
	}
}
