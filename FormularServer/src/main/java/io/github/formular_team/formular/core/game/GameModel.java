package io.github.formular_team.formular.core.game;

import java.util.List;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.math.curve.LineCurve;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.race.RaceConfiguration;

public interface GameModel extends Game {
    // TODO quad tree
    List<LineCurve> getWalls();

    List<KartModel> getKarts();

    List<Driver> getDrivers();

    KartModel createKart(final User user);

    KartModel createKart(final Kart.ControlState state, final User user);

    void addDriver(final Driver driver);

    void addKart(final KartModel kart);

    KartModel removeKart(final int uniqueId);

    KartModel getKart(final int uniqueId);

    void addRace(final Race race);

    Race getRace();

    Race createRace(User user, RaceConfiguration configuration, Course course);

    void step(final float delta);

    void addOnKartAddListener(final OnKartAddListener listener);

    void addOnKartRemoveListener(final OnKartRemoveListener listener);

    void addOnPoseChangeListener(final OnPoseChangeListener listener);

    void addOnRaceAddListener(final OnRaceAddListener listener);

    interface OnKartAddListener {
        void onKartAdd(final Kart kart);
    }

    interface OnKartRemoveListener {
        void onKartRemove(final Kart kart);
    }

    interface OnPoseChangeListener {
        void onPoseChange(final Kart kart);
    }

    interface OnRaceAddListener {
        void onRaceAdd(final Race race);
    }
}
