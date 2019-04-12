package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.color.ColorPalette;

public interface SceneEnvironment {
    int foreground();

    int background();

    ColorPalette extra();
}
