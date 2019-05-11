package io.github.formular_team.formular.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.game.GameModel;
import io.github.formular_team.formular.core.kart.KartDefinition;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.math.curve.LineCurve;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.race.RaceConfiguration;

public final class SimpleGameModel implements GameModel {
    private final List<Driver> drivers = new ArrayList<>();

    private final List<KartModel> karts = new ArrayList<>();

    private final List<Race> races = new ArrayList<>();

    private final List<LineCurve> walls = new ArrayList<>();

    private final List<OnKartAddListener> addListeners = new ArrayList<>();

    private final List<OnKartRemoveListener> removeListeners = new ArrayList<>();

    private final List<OnPoseChangeListener> changeListeners = new ArrayList<>();

    private final List<OnRaceAddListener> raceListeners = new ArrayList<>();

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
        for (final OnRaceAddListener listener : this.raceListeners) {
            listener.onRaceAdd(race);
        }
    }

    // FIXME
    @Override
    public Race getRace() {
        return this.races.stream().findFirst().orElse(null);
    }

    @Override
    public Race createRace(final User user, final RaceConfiguration configuration, final Course course) {
        final Race race = Race.create(this, user, configuration, course);
        this.addRace(race);
        final List<? extends Checkpoint> checkpoints = course.getTrack().getCheckpoints();
        for (int i = 0; i < checkpoints.size(); i++) {
            this.walls.add(new LineCurve(checkpoints.get(i).getP1(), checkpoints.get((i + 1) % checkpoints.size()).getP1()));
            this.walls.add(new LineCurve(checkpoints.get(i).getP2(), checkpoints.get((i + 1) % checkpoints.size()).getP2()));
        }
        return race;
    }

    @Override
    public void step(final float delta) {
        for (final Driver driver : this.drivers) {
            driver.step(delta);
        }
        this.stepPhysics(delta);
        // TODO: good collision
        /*for (final KartModel kart : this.karts) {
            for (final LineCurve wall : this.walls) {
                kart.collide(wall, delta);
            }
        }*/
        // TODO: optimized onPoseChange
        for (final KartModel kart : this.karts) {
            for (final OnPoseChangeListener listener : this.changeListeners) {
                listener.onPoseChange(kart);
            }
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }

    private void stepPhysics(final float delta) {
        final float targetDt = 0.01F;
        final int steps = Math.max((int) (delta / targetDt), 1);
        final float dt = delta / steps;
        for (int n = 0; n < steps; n++) {
            this.stepKarts(dt);
        }
        // temp
        for (final KartModel kart : this.karts) {
            for (final KartModel other : this.karts) {
                if (kart != other) {
                    final Vector2 d = other.getPosition().clone().sub(kart.getPosition());
                    final float r = 1.3F;
                    if (d.length() < r) {
                        if (d.length() == 0.0F) {
                            d.set(1.0F, 0.0F);
                        }
                        d.setLength(r - d.length());
                        kart.getPosition().sub(d);
                        other.getPosition().add(d);
                    }
                }
            }
        }
    }

    private void stepKarts(final float dt) {
        for (final KartModel kart : this.karts) {
            kart.step(dt);
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

    @Override
    public void addOnRaceAddListener(final OnRaceAddListener listener) {
        this.raceListeners.add(listener);
    }
}
