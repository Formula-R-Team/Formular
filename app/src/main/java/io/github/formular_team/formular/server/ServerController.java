package io.github.formular_team.formular.server;

import com.google.common.util.concurrent.Uninterruptibles;

import io.github.formular_team.formular.server.command.StopCommand;

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
        Uninterruptibles.joinUninterruptibly(this.thread);
    }

    public static ServerController create(final Server server) {
        return new ServerController(server, new Thread(server));
    }
}
