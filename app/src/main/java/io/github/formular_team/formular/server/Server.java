package io.github.formular_team.formular.server;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.Consumer;

public interface Server extends Runnable {
    <V> ListenableFuture<V> submitJob(final Job<V> job);

    default <V> V awaitJob(final Job<V> job) {
        return Futures.getUnchecked(this.submitJob(job));
    }

    /**
     * <pre>
     *  while (this.game.isRunning()) {
     *      synchronized (this.pendingTasks) {
     *          for (FutureTask<?> task; (task = this.pendingTasks.poll()) != null; ) {
     *              this.runTask(task);
     *          }
     *      }
     *      final float delta = ...;
     *      final float idle = ...;
     *      this.game.step(delta);
     *      Thread.sleep(idle);
     *  }
     *  </pre>
     */
    @Override
    void run();

    interface Job<V> {
        V call(final Game game);

        static Job<Unit> of(final Consumer<Game> consumer) {
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
        Builder game(final Game game);

        Builder ups(final int ups);

        Server build();
    }
}
