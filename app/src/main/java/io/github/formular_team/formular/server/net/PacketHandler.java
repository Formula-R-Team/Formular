package io.github.formular_team.formular.server.net;

import io.github.formular_team.formular.server.Game;

public interface PacketHandler<T extends Game> {
    void handle(final T game);
}
