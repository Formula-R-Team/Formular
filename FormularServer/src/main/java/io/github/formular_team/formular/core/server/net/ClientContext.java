package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.server.Client;

public class ClientContext implements Context {
    private final Client client;

    public ClientContext(final Client client) {
        this.client = client;
    }

    public Client getClient() {
        return this.client;
    }
}
