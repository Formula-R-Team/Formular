package io.github.formular_team.formular.server;

import io.github.formular_team.formular.User;
import io.github.formular_team.formular.server.net.Packet;

public final class CpuDriver implements Driver {
    private final User user;

    private final KartModel kart;

    private CpuDriver(final User user, final KartModel kart) {
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
    public void step(final float delta) {

    }

    @Override
    public void send(final Packet packet) {

    }

    public static CpuDriver create(final User user, final KartModel kart) {
        return new CpuDriver(user, kart);
    }
}
