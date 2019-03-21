package io.github.formular_team.formular.server;

import io.github.formular_team.formular.User;
import io.github.formular_team.formular.server.net.Packet;

public interface Driver {
    User getUser();

    KartModel getVehicle();

    void step(final float delta);

    void send(final Packet packet);
}
