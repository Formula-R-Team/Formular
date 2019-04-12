package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.Game;

public interface PacketHandler<T extends Game> {
    void handle(final T game);
}
