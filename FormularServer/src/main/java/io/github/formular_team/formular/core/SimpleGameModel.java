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

    private final List<OnKartAddListener> addListeners = new ArrayList<>();

    private final List<OnKartRemoveListener> removeListeners = new ArrayList<>();

    private final List<OnPoseChangeListener> changeListeners = new ArrayList<>();

    private int nextKartId = 0;

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
    public KartModel createKart() {
        return new KartModel(this.nextKartId++, KartDefinition.createKart2());
    }

    @Override
    public void addKart(final KartModel kart) {
        this.karts.add(kart);
        for (final OnKartAddListener listener : this.addListeners) {
            listener.onKartAdd(kart);
        }
    }

    @Override
    public KartModel removeKart(final int uniqueId) {
        final Iterator<KartModel> it = this.karts.iterator();
        while (it.hasNext()) {
            final KartModel kart = it.next();
            if (kart.getUniqueId() == uniqueId) {
                it.remove();
                for (final OnKartRemoveListener listener : this.removeListeners) {
                    listener.onKartRemove(kart);
                }
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
            for (final OnPoseChangeListener listener : this.changeListeners) {
                listener.onPoseChange(kart);
            }
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }

    @Override
    public void addOnKartAddListener(final OnKartAddListener listener) {
        this.addListeners.add(listener);
    }

    @Override
    public void addOnKartRemoveListener(final OnKartRemoveListener listener) {
        this.removeListeners.add(listener);
    }

    @Override
    public void addOnPoseChangeListener(final OnPoseChangeListener listener) {
        this.changeListeners.add(listener);
    }
}
