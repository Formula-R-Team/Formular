package io.github.formular_team.formular.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Connection;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.Protocol;
import io.github.formular_team.formular.core.server.net.StateManager;

// TODO not duplicate server
public final class SimpleClient implements Client {
    private static final Logger LOGGER = Logger.getLogger("SimpleServer");

    private final Selector selector;

    private final InetSocketAddress remoteAddress;

    private final StateManager factory;

    private final User user;

    private final GameView game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private boolean running = true;

    private SimpleClient(final Selector selector, final InetSocketAddress remoteAddress, final StateManager factory, final User user, final GameView game, final BlockingQueue<RunnableFuture<?>> queue, final long ups) {
        this.selector = selector;
        this.remoteAddress = remoteAddress;
        this.factory = factory;
        this.user = user;
        this.game = game;
        this.queue = queue;
        this.ups = ups;
    }

    @Override
    public GameView getGame() {
        return this.game;
    }

    @Override
    public <V> Future<V> submitJob(final Job<? super Client, V> job) {
        final FutureTask<V> futureJob = new FutureTask<>(() -> job.call(this));
        this.addJob(futureJob);
        return futureJob;
    }

    @Override
    public void stop() {
        this.running = false;
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
        final SocketChannel socket;
        try {
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.register(this.selector, SelectionKey.OP_CONNECT);
            socket.connect(this.remoteAddress);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final RunnableFuture<?> STEP_PILL = new FutureTask<>(() -> null);
        final long timeout = 1000 / this.ups;
        for (long past = this.currentTimeMillis(), present = past; this.running; past = present) {
            final long duration = present - past;
            final long deadline = present + timeout;
            this.addJob(STEP_PILL);
//            this.game.step(duration / 1000.0F);
            for (RunnableFuture<?> job; (job = this.pollJob()) != null && job != STEP_PILL; job.run());
            try {
                while ((present = this.currentTimeMillis()) < deadline) {
                    this.selector.select(deadline - present);
                    final Set<SelectionKey> keys = this.selector.selectedKeys();
                    for (final Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); it.remove()) {
                        final SelectionKey key = it.next();
                        if (key.isConnectable()) {
                            this.connect(this.selector, socket, key);
                        } else {
                            key.interestOps(0);
                            if (key.isReadable()) {
                                this.read(key);
                            }
                            if (key.isWritable()) {
                                this.write(key);
                            }
                        }
                    }
                }
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Network error", e);
                this.running = false;
            }
        }
        try {
            socket.close();
        } catch (final IOException ignored) {}
        try {
            this.selector.close();
        } catch (final IOException ignored) {}
    }

    @Override
    public void send(final Packet packet) {
        for (final SelectionKey key: this.selector.keys()) {
            final Object att = key.attachment();
            if (att instanceof Connection) {
                ((Connection) att).send(packet);
            }
        }
    }

    private void connect(final Selector selector, final SocketChannel socket, final SelectionKey key) throws IOException {
        if (!socket.finishConnect()) {
            throw new AssertionError();
        }
        key.interestOps(SelectionKey.OP_READ);
        key.attach(new Connection(key, this.factory.create(new ClientContext(this))));
        LOGGER.info("Connection established");
    }

    private void read(final SelectionKey key) throws IOException {
        this.connection(key).read((SocketChannel) key.channel(), key);
    }

    private void write(final SelectionKey key) throws IOException {
        this.connection(key).write((SocketChannel) key.channel(), key);
    }

    private Connection connection(final SelectionKey key) {
        return (Connection) key.attachment();
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static SimpleClient open(final InetSocketAddress remote, final User user, final GameView game, final long ups) throws IOException {
        return new SimpleClient(Selector.open(), remote, Protocol.createConnectionFactory(), user, game, new LinkedBlockingDeque<>(), ups);
    }
}
