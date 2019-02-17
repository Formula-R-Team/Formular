package io.github.formular_team.formular.color;

import com.google.common.collect.Range;

import java.util.Random;

public interface PaletteFactory {
    ColorPalette create(final Random rng);

    interface Builder {
        Builder size(final Range<Integer> size);

        Builder color(final ColorRange color);

        PaletteFactory build();
    }
}
