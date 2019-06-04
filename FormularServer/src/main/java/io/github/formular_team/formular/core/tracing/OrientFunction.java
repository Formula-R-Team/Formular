package io.github.formular_team.formular.core.tracing;

/**
 * An object which produces an angle in radians indicating the orientation within a given {@link Mapper}.
 */
public interface OrientFunction {
	/**
	 * Applies this function to the given image. The result may be <code>NaN</code>, indicating the orientation is indeterminate.
	 *
	 * @param image image to determine orientation
	 * @return orientation in radians
	 */
	float getOrientation(final Mapper image);
}
