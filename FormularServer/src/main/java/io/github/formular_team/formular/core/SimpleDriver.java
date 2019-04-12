package io.github.formular_team.formular.core;

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

    public static SimpleDriver create(final User user, final KartModel kart) {
        return new SimpleDriver(user, kart);
    }
}
