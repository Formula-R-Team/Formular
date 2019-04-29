package io.github.formular_team.formular.core.server.net;

public class Context {
    private final Connection remote;

    public Context(final Connection remote) {
        this.remote = remote;
    }

    public Connection getRemote() {
        return this.remote;
    }
}
