package io.github.formular_team.formular;

import java.util.List;

import io.github.formular_team.formular.math.LineCurve;
import io.github.formular_team.formular.server.KartModel;

public interface GameModel extends Game {
    // TODO quad tree
    List<LineCurve> getWalls();

    void addKart(final KartModel kart);

    KartModel removeKart(final int uniqueId);

    KartModel getKart(final int uniqueId);

    void step(final float delta);
}
