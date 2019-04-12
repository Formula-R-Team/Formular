package io.github.formular_team.formular.core.server.command;

import io.github.formular_team.formular.core.SimpleGameModel;
import io.github.formular_team.formular.core.server.Server;

public final class StopCommand implements Server.Command<Void> {
    @Override
    public Void call(final SimpleGameModel game) {
        game.stop();
        return null;
    }
}
