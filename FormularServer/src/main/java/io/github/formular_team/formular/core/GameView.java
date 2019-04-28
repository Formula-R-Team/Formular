package io.github.formular_team.formular.core;

import java.util.Optional;

public interface GameView extends Game {
    RacerStatus getStatus();

    Kart createKart(final int uniqueId);

    Optional<Kart> removeKart(final int uniqueId);

    Optional<Kart> getKart(final int uniqueId);
}
