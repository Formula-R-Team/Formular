package io.github.formular_team.formular.server;

public interface Kart {
    void shift(final Gear position);

    void step(final float delta);

    enum Gear {
        PARK,
        REVERSE,
        NEUTRAL,
        DRIVE
    }
}
