package io.github.formular_team.formular.core.name;

import io.github.formular_team.formular.core.SceneEnvironment;
import io.github.formular_team.formular.core.math.curve.Path;

public interface NameSuggestionProvider {
    String create(final SceneEnvironment environment, final Path road);
}
