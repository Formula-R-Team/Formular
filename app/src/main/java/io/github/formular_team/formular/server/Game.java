package io.github.formular_team.formular.server;

import com.google.common.collect.Lists;

import java.util.List;

import io.github.formular_team.formular.car.KartModel;

public final class Game {
    private final List<Driver> drivers = Lists.newArrayList();

    private final List<KartModel> karts = Lists.newArrayList();

    private final List<Race> races = Lists.newArrayList();

    public boolean isRunning() {
        return true;
    }

    public void step(final float delta) {
        for (final KartModel kart : this.karts) {
            kart.step(delta);
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }

    public void stop() {}
}
