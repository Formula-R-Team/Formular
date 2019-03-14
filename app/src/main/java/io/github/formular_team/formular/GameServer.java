package io.github.formular_team.formular;

import io.github.formular_team.formular.car.KartModel;

public interface GameServer extends Game {
    void addKart(final KartModel kart);

    KartModel removeKart(final int uniqueId);

    KartModel getKart(final int uniqueId);
}
