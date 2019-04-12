package io.github.formular_team.formular.core;

public interface Driver {
    User getUser();

    KartModel getVehicle();

    void step(final float delta);
}
