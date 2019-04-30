package io.github.formular_team.formular.core.server.net;

public interface Connection {
    void send(final Packet packet);

    void close();
}
