package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.server.Client;

public class ClientContext extends Context {
    private final Client client;

    public ClientContext(final Context context, final Client client) {
        super(context.getRemote());
        this.client = client;
    }

    public Client getClient() {
        return this.client;
    }
}
