package io.github.formular_team.formular.server;

import io.github.formular_team.formular.color.ColorPalette;

public interface SceneEnvironment {
    int foreground();

    int background();

    ColorPalette extra();
}
