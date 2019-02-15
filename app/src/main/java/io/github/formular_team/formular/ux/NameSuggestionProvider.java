package io.github.formular_team.formular.ux;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.scene.SceneEnvironment;

public interface NameSuggestionProvider {
    String create(final SceneEnvironment environment, final Path road);
}
