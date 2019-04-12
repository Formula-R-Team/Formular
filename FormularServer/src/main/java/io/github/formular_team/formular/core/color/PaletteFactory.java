package io.github.formular_team.formular.core.color;

import java.util.Random;

public interface PaletteFactory {
    ColorPalette create(final Random rng);

    interface Builder {
        Builder size(final int size);

        Builder color(final ColorRange color);

        PaletteFactory build();
    }
}
