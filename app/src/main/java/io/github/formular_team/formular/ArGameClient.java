package io.github.formular_team.formular;

import io.github.formular_team.formular.server.Kart;

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
