package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;

public final class TransformMapper implements Mapper {
	private final Mapper image;

	private float x;

	private float y;

	private float cosRotation;

	private float sinRotation;

	private float rotation;

	public TransformMapper(final Mapper image, final float x, final float y, final float rotation) {
		this.image = image;
		this.setTranslation(x, y);
		this.setRotation(rotation);
	}

	public void setTranslation(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public void setRotation(final float rotation) {
	    this.rotation = rotation;
		this.cosRotation = Mth.cos(rotation);
		this.sinRotation = Mth.sin(rotation);
	}

	@Override
	public float get(final float x, final float y) {
		final Vector2 v = this.transformPoint(new Vector2(x, y));
		return this.image.get(v.getX(), v.getY());
	}

	public Vector2 transformPoint(final Vector2 vector) {
		final Vector2 result = this.transformVec(vector);
		result.add(new Vector2(this.x, this.y));
		return result;
	}

	public Vector2 transformVec(final Vector2 vector) {
		// TODO: use matrices
		return new Vector2(vector.getX() * this.cosRotation - vector.getY() * this.sinRotation, vector.getY() * this.cosRotation + vector.getX() * this.sinRotation);
	}

	public Matrix3 getMatrix() {
	    return new Matrix3().rotate(this.rotation).translate(this.x, this.y);
    }
}
