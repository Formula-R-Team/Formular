package io.github.formular_team.formular.color;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MyColorPalette implements ColorPalette {
    private final int[] palette;

    MyColorPalette(final int[] palette){
        this.palette = checkNotNull(palette);
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
    public int get(final int index) {
        return this.palette[checkElementIndex(index, this.size())];
    }

    @Override
    public IntStream stream() {
        return Arrays.stream(this.palette);
    }
}
