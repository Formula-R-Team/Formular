package io.github.formular_team.formular.core.server.net;

import java.util.Objects;

import io.github.formular_team.formular.core.server.Server;

public class ServerContext implements Context {
    private final Server server;

    public ServerContext(final Server server) {
        this.server = Objects.requireNonNull(server);
    }

    public Server getServer() {
        return this.server;
    }
}
