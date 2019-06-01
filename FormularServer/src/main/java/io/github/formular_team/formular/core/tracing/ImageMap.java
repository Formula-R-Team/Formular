package io.github.formular_team.formular.core.tracing;

/**
 * An object that provides a view of image color data within the bounds of width and height.
 */
public interface ImageMap {
    /**
     * Returns the width of this image map
     *
     * @return width of this image map
     */
    int width();

    /**
     * Returns the height of this image map
     *
     * @return height of this image map
     */
    int height();

    /**
     * Returns the color at the specified location. Throws an exception if x or y are out of bounds. The returned color is a non-premultiplied ARGB value in sRGB color space.
     *
     * @param x x coordinate (0, width] of the pixel to return
     * @param y y coordinate (0, height] of the pixel to return
     * @return argb color at the specified coordinate
     */
    int get(final int x, final int y);
}
