package io.github.formular_team.formular.core.color;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public final class SimpleColorPalette implements ColorPalette {
    private final Color[] palette;

    SimpleColorPalette(final Color[] palette){
        this.palette = Objects.requireNonNull(palette);
    }

    @Override
    public int size() {
        return this.palette.length;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public Color get(final int index) {
        return this.palette[index];
    }

    @Override
    public Stream<Color> stream() {
        return Arrays.stream(this.palette);
    }
}
