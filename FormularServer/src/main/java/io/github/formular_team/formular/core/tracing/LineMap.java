package io.github.formular_team.formular.core.tracing;

/**
 * An object which supplies a probabilistic view of the lines of a two dimensional map.
 */
public interface LineMap {
	/**
	 * Returns the float value at the specified location indicating likeness to line. The value is of the range (0..1) from least to most likely.
	 *
	 * @param x x coordinate of the line probability to return
	 * @param y y coordinate of the line probability to return
	 * @return line probability at the specified coordinate
	 */
	float get(final int x, final int y);
}
