package io.github.formular_team.formular.server;

public interface Kart {
    void shift(final Gear position);

    void steer(final float angle);

    void step(final float delta);

    enum Gear {
        PARK,
        REVERSE,
        NEUTRAL,
        DRIVE
    }
}
