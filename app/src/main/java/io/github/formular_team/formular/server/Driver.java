package io.github.formular_team.formular.server;

public interface Driver {
    Kart vehicle();

    void step(final float delta);
}
