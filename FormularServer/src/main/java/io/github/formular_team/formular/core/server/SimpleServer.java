package io.github.formular_team.formular.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.core.SimpleGameModel;

public final class SimpleServer implements Server {
    private final InetSocketAddress address;

    private final SimpleGameModel game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private SimpleServer(final InetSocketAddress address, final SimpleGameModel game, final long ups) {
        this(address, game, new LinkedBlockingDeque<>(), ups);
    }

    private SimpleServer(final InetSocketAddress address, final SimpleGameModel game, final BlockingQueue<RunnableFuture<?>> queue, final long ups) {
        this.address = address;
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
        final Selector selector;
        final ServerSocketChannel serverSocket;
        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(this.address);
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final RunnableFuture<?> STEP = new FutureTask<>(() -> null);
        final long timeout = 1000 / this.ups;
        for (long past = this.currentTimeMillis(), present = past; this.game.isRunning(); past = present) {
            final long duration = present - past;
            final long deadline = present + timeout;
            this.addJob(STEP);
            this.game.step(duration / 1000.0F);
            try {
                this.select(selector, serverSocket);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            for (RunnableFuture<?> job; (job = this.pollJob()) != null && job != STEP; job.run());
            try {
                for (RunnableFuture<?> job; (present = this.currentTimeMillis()) < deadline && (job = this.pollJob(deadline - present)) != null; job.run());
            } catch (final InterruptedException e) {
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (final IOException ignored) {}
        try {
            selector.close();
        } catch (final IOException ignored) {}
    }

    private void select(final Selector selector, final ServerSocketChannel socket) throws IOException {
        selector.select();
        final Set<SelectionKey> selectedKeys = selector.selectedKeys();
        final Iterator<SelectionKey> it = selectedKeys.iterator();
        while (it.hasNext()) {
            final SelectionKey key = it.next();
            if (key.isAcceptable()) {
                // register
            }
            if (key.isReadable()) {
                // read
            }
            it.remove();
        }
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static SimpleServer create(final InetSocketAddress address, final SimpleGameModel game, final long ups) {
        return new SimpleServer(address, game, ups);
    }
}
