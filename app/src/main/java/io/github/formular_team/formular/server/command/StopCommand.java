package io.github.formular_team.formular.server.command;

import io.github.formular_team.formular.server.Game;
import io.github.formular_team.formular.server.Server;

public final class StopCommand implements Server.Command<Void> {
    @Override
    public Void call(final Game game) {
        game.stop();
        return null;
    }
}
