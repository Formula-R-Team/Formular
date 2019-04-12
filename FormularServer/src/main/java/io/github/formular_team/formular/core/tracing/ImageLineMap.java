package io.github.formular_team.formular.core.tracing;

public final class ImageLineMap implements LineMap {
    private final ImageMap image;

    public ImageLineMap(final ImageMap image) {
        this.image = image;
    }

    @Override
    public float get(final int x, final int y) {
        if (x >= 0 && y >= 0 && x < this.image.width() & y < this.image.height()) {
            final int rgb = this.image.get(x, y);
            if ((rgb & 0xFF000000) != 0) {
                final int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
                return 1.0F - (float) (r > g && r > b ? r : g > b ? g : b) / 0xFF;
            }
        }
        return 0.0F;
    }
}
