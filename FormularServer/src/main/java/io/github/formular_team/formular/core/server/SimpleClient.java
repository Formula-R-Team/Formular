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

import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.game.GameView;
import io.github.formular_team.formular.core.race.RaceConfiguration;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.ContextualPacketGraph;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.Protocol;
import io.github.formular_team.formular.core.server.net.SimpleConnection;
import io.github.formular_team.formular.core.server.net.serverbound.BeginRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.ControlPacket;
import io.github.formular_team.formular.core.server.net.serverbound.CreateRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.JoinRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.NewUserPacket;

// TODO not duplicate server
public final class SimpleClient implements Client {
    private static final Logger LOGGER = Logger.getLogger("SimpleServer");

    private final Selector selector;

    private final InetSocketAddress remoteAddress;

    private final ContextualPacketGraph factory;

    private final User user;

    private final GameView game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private boolean running = true;

    private SimpleClient(final Selector selector, final InetSocketAddress remoteAddress, final ContextualPacketGraph factory, final User user, final GameView game, final BlockingQueue<RunnableFuture<?>> queue, final long ups) {
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
    public void createRace(final RaceConfiguration config, final Course course) {
        this.send(new CreateRacePacket(config, course));
    }

    @Override
    public void joinRace() {
        this.send(new JoinRacePacket());
    }

    @Override
    public void startRace() {
        this.send(new BeginRacePacket());
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

    private State state = new WaitingConnect();

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
        final long timeout = 1000 / this.ups;
        for (long past = this.currentTimeMillis(), present; this.running; past = present) {
            present = this.currentTimeMillis();
            final long duration = present - past;
            final long deadline = present + timeout;
            this.state.update(duration / 1000.0F);
            try {
                for (long now; (now = this.currentTimeMillis()) < deadline; ) {
                    this.selector.select(deadline - now);
                    final Set<SelectionKey> keys = this.selector.selectedKeys();
                    for (final Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); it.remove()) {
                        final SelectionKey key = it.next();
                        if (key.isConnectable()) {
                            this.connect(this.selector, socket, key);
                        } else {
                            if (key.isReadable()) {
                                this.read(key);
                            }
                            if (key.isValid() && key.isWritable()) {
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

    abstract class State {
        abstract void send(final Packet packet);

        abstract void update(float delta);
    }

    class WaitingConnect extends State {
        @Override
        void send(final Packet packet) {
            throw new IllegalStateException("Cannot send " + packet.getClass().getName());
        }

        @Override
        void update(final float delta) {}
    }

    class Connected extends State {
        final SimpleConnection connection;

        final RunnableFuture<?> pill = new FutureTask<>(() -> null);

        Connected(final SimpleConnection connection) {
            this.connection = connection;
        }

        SimpleClient client() {
            return SimpleClient.this;
        }

        @Override
        void send(final Packet packet) {
            this.connection.send(packet);
        }

        @Override
        void update(final float delta) {
            this.client().send(new ControlPacket(this.client().game.getControlState()));
            this.client().addJob(this.pill);
            for (RunnableFuture<?> job; (job = this.client().pollJob()) != null && job != this.pill; job.run());
        }
    }

    @Override
    public void send(final Packet packet) {
        this.state.send(packet);
    }

    private void connect(final Selector selector, final SocketChannel socket, final SelectionKey key) throws IOException {
        if (!socket.finishConnect()) {
            throw new AssertionError();
        }
        key.interestOps(SelectionKey.OP_READ);
        final SimpleConnection connection = new SimpleConnection(key, this.factory.create());
        connection.setContext(this.factory.create(new ClientContext(new Context(connection), this)));
        key.attach(connection);
        LOGGER.info("Connection established");
        this.state = new Connected(connection);
        this.send(new NewUserPacket(this.user));
    }

    private void read(final SelectionKey key) throws IOException {
        this.connection(key).read((SocketChannel) key.channel(), key);
    }

    private void write(final SelectionKey key) throws IOException {
        this.connection(key).write((SocketChannel) key.channel(), key);
    }

    private SimpleConnection connection(final SelectionKey key) {
        return (SimpleConnection) key.attachment();
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static SimpleClient open(final InetSocketAddress remote, final User user, final GameView game, final long ups) throws IOException {
        return new SimpleClient(Selector.open(), remote, Protocol.createConnectionFactory(), user, game, new LinkedBlockingDeque<>(), ups);
    }
}
