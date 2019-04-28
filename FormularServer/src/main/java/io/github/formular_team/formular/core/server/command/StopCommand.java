package io.github.formular_team.formular.core.server.command;

import io.github.formular_team.formular.core.server.Endpoint;

public final class StopCommand<T extends Endpoint<?>> implements Endpoint.Command<T, Void> {
    @Override
    public Void call(final T endpoint) {
        endpoint.stop();
        return null;
    }
}
