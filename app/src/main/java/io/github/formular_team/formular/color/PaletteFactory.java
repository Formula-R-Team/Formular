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

    interface ColorRange {
        Range<Float> red();

        Range<Float> green();

        Range<Float> blue();

        Range<Float> hue();

        Range<Float> saturation();

        Range<Float> value();

        interface Builder {
            Builder red(final Range<Float> red);

            Builder green(final Range<Float> green);

            Builder blue(final Range<Float> blue);

            Builder hue(final Range<Float> hue);

            Builder saturation(final Range<Float> saturation);

            Builder value(final Range<Float> value);

            ColorRange build();
        }
    }
}
