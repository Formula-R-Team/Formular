package io.github.formular_team.formular.scene;

import io.github.formular_team.formular.color.ColorPalette;

public interface SceneEnvironment {
    int foreground();

    int background();

    ColorPalette extra();
}
