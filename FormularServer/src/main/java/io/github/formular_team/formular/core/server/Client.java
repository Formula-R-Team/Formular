package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.GameView;

public interface Client extends Endpoint<Client> {
    GameView getGame();
}
