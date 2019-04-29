package io.github.formular_team.formular.core.server.net;

import java.util.Objects;

import io.github.formular_team.formular.core.server.Server;

public class ServerContext extends Context {
    private final Server server;

    public ServerContext(final Context context, final Server server) {
        super(context.getRemote());
        this.server = Objects.requireNonNull(server);
    }

    public Server getServer() {
        return this.server;
    }
}
