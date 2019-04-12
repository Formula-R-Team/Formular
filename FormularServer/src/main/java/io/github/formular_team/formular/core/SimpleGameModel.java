package io.github.formular_team.formular.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.formular_team.formular.core.math.LineCurve;
import io.github.formular_team.formular.core.race.Race;

public final class SimpleGameModel implements GameModel {
    private final List<Driver> drivers = new ArrayList<>();

    private final List<KartModel> karts = new ArrayList<>();

    private final List<Race> races = new ArrayList<>();

    private final List<LineCurve> walls = new ArrayList<>();

    public boolean isRunning() {
        return true;
    }

    @Override
    public List<LineCurve> getWalls() {
        return this.walls;
    }

    @Override
    public List<Driver> getDrivers() {
        return this.drivers;
    }

    @Override
    public void addDriver(final Driver driver) {
        this.drivers.add(driver);
    }

    @Override
    public List<KartModel> getKarts() {
        return this.karts;
    }

    public void stop() {}

    @Override
    public void addKart(final KartModel kart) {
        this.karts.add(kart);
    }

    @Override
    public KartModel removeKart(final int uniqueId) {
        final Iterator<KartModel> it = this.karts.iterator();
        while (it.hasNext()) {
            final KartModel kart = it.next();
            if (kart.getUniqueId() == uniqueId) {
                it.remove();
                return kart;
            }
        }
        return null;
    }

    @Override
    public KartModel getKart(final int uniqueId) {
        for (final KartModel kart : this.karts) {
            if (kart.getUniqueId() == uniqueId) {
                return kart;
            }
        }
        return null;
    }

    @Override
    public void addRace(final Race race) {
        this.races.add(race);
    }

    @Override
    public void step(final float delta) {
        for (final Driver driver : this.drivers) {
            driver.step(delta);
        }
        for (final KartModel kart : this.karts) {
            kart.step(delta);
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }
}
