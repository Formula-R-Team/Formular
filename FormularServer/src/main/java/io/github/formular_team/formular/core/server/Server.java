package io.github.formular_team.formular.core.server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import io.github.formular_team.formular.core.SimpleGameModel;

public interface Server extends Runnable {
    <V> Future<V> submitJob(final Job<V> job);

    default <V> V awaitJob(final Job<V> job) {
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
            throw new AssertionError();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    void run();

    interface Job<V> {
        V call(final SimpleGameModel game);

        static Job<Unit> of(final Consumer<SimpleGameModel> consumer) {
            return game -> {
                consumer.accept(game);
                return Unit.INSTANCE;
            };
        }

        final class Unit {
            private static final Unit INSTANCE = new Unit();

            private Unit() {}
        }
    }

    interface Command<V> extends Job<V> {}

    interface Builder {
        Builder game(final SimpleGameModel game);

        Builder ups(final int ups);

        Server build();
    }
}
