package io.github.formular_team.formular.core.tracing;

public interface ImageMap {
    int width();

    int height();

    int get(final int x, final int y);
}
