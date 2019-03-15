package io.github.formular_team.formular.server;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import io.github.formular_team.formular.GameModel;
import io.github.formular_team.formular.math.LineCurve;

public final class SimpleGameModel implements GameModel {
    private final List<Driver> drivers = Lists.newArrayList();

    private final List<KartModel> karts = Lists.newArrayList();

    private final List<Race> races = Lists.newArrayList();

    private final List<LineCurve> walls = Lists.newArrayList();

    public boolean isRunning() {
        return true;
    }

    @Override
    public void step(final float delta) {
        for (final KartModel kart : this.karts) {
            kart.step(delta);
        }
        for (final Race race : this.races) {
            race.step(delta);
        }
    }

    @Override
    public List<LineCurve> getWalls() {
        return this.walls;
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
}
