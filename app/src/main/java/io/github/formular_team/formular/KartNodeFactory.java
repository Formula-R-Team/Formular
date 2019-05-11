package io.github.formular_team.formular;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.kart.KartView;

public interface KartNodeFactory {
    KartNode create(final KartView kart);
}
