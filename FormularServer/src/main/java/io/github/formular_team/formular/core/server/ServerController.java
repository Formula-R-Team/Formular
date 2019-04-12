package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.server.command.StopCommand;

public final class ServerController {
    private final Server server;

    private final Thread thread;

    private ServerController(final Server server, final Thread thread) {
        this.server = server;
        this.thread = thread;
    }

    public void start() {
        this.thread.start();
        this.server.awaitJob(Server.Job.of(game -> {}));
    }

    public void stop() {
        this.server.submitJob(new StopCommand());
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

    public static ServerController create(final Server server) {
        return new ServerController(server, new Thread(server));
    }
}
