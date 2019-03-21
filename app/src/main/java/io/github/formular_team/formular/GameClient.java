package io.github.formular_team.formular;

import io.github.formular_team.formular.server.Kart;

public interface GameClient extends Game {
    RacerStatus getStatus();

    void addKart(final Kart kart);

    Kart removeKart(final int uniqueId);

    Kart getKart(final int uniqueId);
}
