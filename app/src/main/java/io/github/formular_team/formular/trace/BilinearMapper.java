package io.github.formular_team.formular.trace;

public final class BilinearMapper implements Mapper {
	private final LineMap image;

	public BilinearMapper(final LineMap image) {
		this.image = image;
	}

	@Override
	public float get(final float x, final float y) {
		final int ix = (int) x;
		final int iy = (int) y;
		final float fx = x - ix;
		final float fy = y - iy;
		final float v00 = this.image.get(ix, iy);
		if (fx == 0.0D && fy == 0.0D) {
			return v00;
		}
		final float v10 = this.image.get(ix + 1, iy);
		final float v01 = this.image.get(ix, iy + 1);
		final float v11 = this.image.get(ix + 1, iy + 1);
		return this.blerp(v00, v10, v01, v11, fx, fy);
	}

	private float blerp(final float v00, final float v10, final float v01, final float v11, final float tx, final float ty) {
		return this.lerp(this.lerp(v00, v10, tx), this.lerp(v01, v11, tx), ty);
	}

	private float lerp(final float v0, final float v1, final float t) {
		return (1.0F - t) * v0 + t * v1;
	}
}
