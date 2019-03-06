package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Vector2;

public final class TransformMapper implements Mapper {
	private final Mapper image;

	private float x;

	private float y;

	private float cosRotation;

	private float sinRotation;

	public TransformMapper(final Mapper image, final float x, final float y, final float rotation) {
		this(image, x, y, (float) Math.cos(rotation), (float) Math.sin(rotation));
	}

	private TransformMapper(final Mapper image, final float x, final float y, final float cosRotation, final float sinRotation) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.cosRotation = cosRotation;
		this.sinRotation = sinRotation;
	}

	public void setX(final float x) {
		this.x = x;
	}

	public void setY(final float y) {
		this.y = y;
	}

	public void setRotation(final float rotation) {
		this.cosRotation = (float) Math.cos(rotation);
		this.sinRotation = (float) Math.sin(rotation);
	}

	@Override
	public float get(final float x, final float y) {
		final Vector2 v = this.transformPoint(new Vector2(x, y));
		return this.image.get(v.x(), v.y());
	}

	public Vector2 transformPoint(final Vector2 vector) {
		final Vector2 result = this.transformVec(vector);
		result.add(new Vector2(this.x, this.y));
		return result;
	}

	public Vector2 transformVec(final Vector2 vector) {
		return new Vector2(vector.x() * this.cosRotation - vector.y() * this.sinRotation, vector.y() * this.cosRotation + vector.x() * this.sinRotation);
	}
}
