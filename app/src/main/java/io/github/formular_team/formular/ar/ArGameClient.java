package io.github.formular_team.formular.ar;

import io.github.formular_team.formular.server.GameClient;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.RacerStatus;

public class ArGameClient implements GameClient {
    @Override
    public RacerStatus getStatus() {
        return null;
    }

    @Override
    public void addKart(final Kart kart) {

    }

    @Override
    public Kart removeKart(final int uniqueId) {
        return null;
    }

    @Override
    public Kart getKart(final int uniqueId) {
        return null;
    }

    public static ArGameClient create() {
        return new ArGameClient();
    }
}
