package io.github.formular_team.formular;

import java.util.List;

import io.github.formular_team.formular.math.LineCurve;
import io.github.formular_team.formular.server.Driver;
import io.github.formular_team.formular.server.KartModel;
import io.github.formular_team.formular.server.Race;

public interface GameModel extends Game {
    // TODO quad tree
    List<LineCurve> getWalls();

    List<KartModel> getKarts();

    List<Driver> getDrivers();

    void addDriver(final Driver driver);

    void addKart(final KartModel kart);

    KartModel removeKart(final int uniqueId);

    KartModel getKart(final int uniqueId);

    void addRace(final Race race);

    void step(final float delta);
}
