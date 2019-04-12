package io.github.formular_team.formular;

import android.graphics.Bitmap;

import io.github.formular_team.formular.core.tracing.ImageMap;

import static com.google.common.base.Preconditions.checkNotNull;

public final class BitmapImageMap implements ImageMap {
    private final Bitmap bitmap;

    public BitmapImageMap(final Bitmap bitmap) {
        this.bitmap = checkNotNull(bitmap);
    }

    @Override
    public int width() {
        return this.bitmap.getWidth();
    }

    @Override
    public int height() {
        return this.bitmap.getHeight();
    }

    @Override
    public int get(final int x, final int y) {
        return this.bitmap.getPixel(x, y);
    }
}
