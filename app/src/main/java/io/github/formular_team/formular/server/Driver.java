package io.github.formular_team.formular.server;

import io.github.formular_team.formular.User;

public interface Driver {
    User getUser();

    KartModel getVehicle();

    void step(final float delta);
}
