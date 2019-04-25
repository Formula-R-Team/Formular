package io.github.formular_team.formular;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.KartModel;

public interface KartNodeFactory {
    KartNode create(final KartModel kart);
}
