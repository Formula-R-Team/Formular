package io.github.formular_team.formular.core.tracing;

/**
 * An object which supplies a two dimensional mapping to float values.
 */
public interface Mapper {
	/**
	 * Returns the float value at the specified location.
	 *
	 * @param x x coordinate of the float to return
	 * @param y y coordinate of the float to return
	 * @return float value at the specified coordinate
	 */
	float get(final float x, final float y);
}
