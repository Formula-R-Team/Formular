package io.github.formular_team.formular;

import android.graphics.Bitmap;

import java.util.Objects;

import io.github.formular_team.formular.core.tracing.ImageMap;

/**
 * An image map which provides a view of a {@link Bitmap}.
 */
public final class BitmapImageMap implements ImageMap {
    private final Bitmap bitmap;

    public BitmapImageMap(final Bitmap bitmap) {
        this.bitmap = Objects.requireNonNull(bitmap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int width() {
        return this.bitmap.getWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int height() {
        return this.bitmap.getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get(final int x, final int y) {
        return this.bitmap.getPixel(x, y);
    }
}
