package io.github.formular_team.formular.server;

import io.github.formular_team.formular.User;
import io.github.formular_team.formular.server.net.Packet;

public final class SimpleDriver implements Driver {
    private final User user;

    private final KartModel kart;

    private SimpleDriver(final User user, final KartModel kart) {
        this.user = user;
        this.kart = kart;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public KartModel getVehicle() {
        return this.kart;
    }

    @Override
    public void step(final float delta) {}

    @Override
    public void send(final Packet packet) {}

    public static SimpleDriver create(final User user, final KartModel kart) {
        return new SimpleDriver(user, kart);
    }
}
