package io.github.formular_team.formular.core.server;

import java.util.concurrent.Future;

import io.github.formular_team.formular.core.server.command.StopCommand;

public final class EndpointController<T extends Endpoint<T>> {
    private final T endpoint;

    private final Thread thread;

    private EndpointController(final T endpoint, final Thread thread) {
        this.endpoint = endpoint;
        this.thread = thread;
    }

    public void start() {
        this.thread.start();
        this.endpoint.awaitJob(Server.Job.of(game -> {}));
    }

    public <V> Future<V> submitJob(final Endpoint.Job<? super T, V> job) {
        return this.endpoint.submitJob(job);
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

    public static <T extends Endpoint<T>> EndpointController<T> create(final T endpoint) {
        return new EndpointController<>(endpoint, new Thread(endpoint));
    }
}
