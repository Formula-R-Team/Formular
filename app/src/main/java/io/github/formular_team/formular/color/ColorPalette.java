package io.github.formular_team.formular.color;

import java.util.stream.IntStream;

public interface ColorPalette {
    int size();

    boolean isEmpty();

    int get(final int index);

    IntStream stream();
}
