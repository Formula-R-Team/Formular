package io.github.formular_team.formular.core.color;

import java.util.stream.Stream;

public interface ColorPalette {
    int size();

    boolean isEmpty();

    Color get(final int index);

    Stream<Color> stream();
}
