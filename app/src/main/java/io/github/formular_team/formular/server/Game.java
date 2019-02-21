package io.github.formular_team.formular.server;

import com.google.common.collect.Lists;

import java.util.List;

public final class Game {
    private final List<Driver> drivers = Lists.newArrayList();

    private final List<Kart> karts = Lists.newArrayList();

    private final List<Race> races = Lists.newArrayList();

    public boolean isRunning() {
        return true;
    }

    public void step(final float delta) {
        for (final Kart kart : this.karts) {
            kart.step(delta);
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }

    public void stop() {}
}
