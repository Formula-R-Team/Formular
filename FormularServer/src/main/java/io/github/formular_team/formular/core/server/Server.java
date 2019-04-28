package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.GameModel;

public interface Server extends Endpoint<Server> {
    GameModel getGame();
}
