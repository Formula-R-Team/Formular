package io.github.formular_team.formular.core.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.core.SimpleGameModel;

public final class SimpleServer implements Server {
    private final SimpleGameModel game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private SimpleServer(final SimpleGameModel game, final long ups) {
        this(game, new LinkedBlockingDeque<>(), ups);
    }

    private SimpleServer(final SimpleGameModel game, final BlockingQueue<RunnableFuture<?>> queue, final long ups) {
        this.game = game;
        this.queue = queue;
        this.ups = ups;
    }

    @Override
    public <V> Future<V> submitJob(final Job<V> job) {
        final FutureTask<V> futureJob = new FutureTask<>(() -> job.call(this.game));
        this.addJob(futureJob);
        return futureJob;
    }

    private void addJob(final RunnableFuture<?> job) {
        this.queue.add(job);
    }

    private RunnableFuture<?> pollJob() {
        return this.queue.poll();
    }

    private RunnableFuture<?> pollJob(final long timeout) throws InterruptedException {
        return this.queue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        final RunnableFuture<?> STEP = new FutureTask<>(() -> null);
        final long timeout = 1000 / this.ups;
        for (long past = this.currentTimeMillis(), present = past; this.game.isRunning(); past = present) {
            final long duration = present - past;
            final long deadline = present + timeout;
            this.addJob(STEP);
            this.game.step(duration / 1000.0F);
            for (RunnableFuture<?> job; (job = this.pollJob()) != null && job != STEP; job.run());
            try {
                for (RunnableFuture<?> job; (present = this.currentTimeMillis()) < deadline && (job = this.pollJob(deadline - present)) != null; job.run());
            } catch (final InterruptedException e) {
                break;
            }
        }
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static SimpleServer create(final SimpleGameModel game, final long ups) {
        return new SimpleServer(game, ups);
    }
}
