package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.kart.KartModel;

public interface Driver {
    User getUser();

    KartModel getVehicle();

    void step(final float delta);
}
