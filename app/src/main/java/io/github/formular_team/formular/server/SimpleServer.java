package io.github.formular_team.formular.server;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public final class SimpleServer implements Server {
    private final Game game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private SimpleServer(final Game game, final long ups) {
        this(game, Queues.newLinkedBlockingDeque(), ups);
    }

    private SimpleServer(final Game game, final BlockingQueue<RunnableFuture<?>> queue, final long ups) {
        this.game = game;
        this.queue = queue;
        this.ups = ups;
    }

    @Override
    public <V> ListenableFuture<V> submitJob(final Job<V> job) {
        final ListenableFutureTask<V> futureJob = ListenableFutureTask.create(() -> job.call(this.game));
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

    public static SimpleServer create(final Game game, final long ups) {
        return new SimpleServer(game, ups);
    }
}
