package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.server.command.StopCommand;

public final class EndpointController {
    private final Endpoint<? extends Endpoint> endpoint;

    private final Thread thread;

    private EndpointController(final Endpoint<? extends Endpoint> endpoint, final Thread thread) {
        this.endpoint = endpoint;
        this.thread = thread;
    }

    public void start() {
        this.thread.start();
        this.endpoint.awaitJob(Server.Job.of(game -> {}));
    }

    public void stop() {
        this.endpoint.submitJob(new StopCommand<>());
        this.join();
    }

    private void join() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    this.thread.join();
                    return;
                } catch (final InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static EndpointController create(final Endpoint<? extends Endpoint> endpoint) {
        return new EndpointController(endpoint, new Thread(endpoint));
    }
}
