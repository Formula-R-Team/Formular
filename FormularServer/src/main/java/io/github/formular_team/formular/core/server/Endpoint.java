package io.github.formular_team.formular.core.server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import io.github.formular_team.formular.core.server.net.Packet;

public interface Endpoint<T> extends Runnable {
    int DEFAULT_PORT = 56250;

    <V> Future<V> submitJob(final Job<? super T, V> job);

    default <V> V awaitJob(final Job<? super T, V> job) {
        final Future<V> future = this.submitJob(job);
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (final InterruptedException e) {
                    interrupted = true;
                }
            }
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void stop();

    interface Job<T, V> {
        V call(final T t);

        static <T> Job<T, Unit> of(final Consumer<T> consumer) {
            return t -> {
                consumer.accept(t);
                return Unit.INSTANCE;
            };
        }

        final class Unit {
            private static final Unit INSTANCE = new Unit();

            private Unit() {}
        }
    }

    interface Command<T, V> extends Job<T, V> {}

    void send(final Packet packet);
}
