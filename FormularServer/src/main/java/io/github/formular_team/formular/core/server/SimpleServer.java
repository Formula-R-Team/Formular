package io.github.formular_team.formular.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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

import io.github.formular_team.formular.core.game.GameModel;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.LanAdvertiser;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.Protocol;
import io.github.formular_team.formular.core.server.net.ServerAdvertisement;
import io.github.formular_team.formular.core.server.net.ServerContext;
import io.github.formular_team.formular.core.server.net.SimpleConnection;
import io.github.formular_team.formular.core.server.net.PacketGraph;
import io.github.formular_team.formular.core.server.net.clientbound.KartAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.RaceAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPosePacket;

public final class SimpleServer implements Server {
    private static final Logger LOGGER = Logger.getLogger("SimpleServer");

    private final Selector selector;

    private final InetSocketAddress address;

    private final PacketGraph<Context> factory;

    private final GameModel game;

    private final BlockingQueue<RunnableFuture<?>> queue;

    private final long ups;

    private boolean running = true;

    private final Thread lan;

    private SimpleServer(final Selector selector, final InetSocketAddress address, final PacketGraph<Context> factory, final GameModel game, final BlockingQueue<RunnableFuture<?>> queue, final long ups, final Thread lan) {
        this.selector = selector;
        this.address = address;
        this.factory = factory;
        this.game = game;
        this.queue = queue;
        this.ups = ups;
        this.lan = lan;
    }

    @Override
    public GameModel getGame() {
        return this.game;
    }

    @Override
    public <V> Future<V> submitJob(final Job<? super Server, V> job) {
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
        final ServerSocketChannel socket;
        try {
            socket = ServerSocketChannel.open();
            socket.configureBlocking(false);
            socket.register(this.selector, SelectionKey.OP_ACCEPT);
            socket.bind(this.address);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.lan.start();
        LOGGER.info("Listening on port " + this.address.getPort());
        this.game.addOnKartAddListener(kart -> this.send(new KartAddPacket(kart)));
        this.game.addOnPoseChangeListener(kart -> this.send(new SetPosePacket(kart)));
        this.game.addOnRaceAddListener(race -> this.send(new RaceAddPacket(race)));
        final RunnableFuture<?> STEP_PILL = new FutureTask<>(() -> null);
        final long timeout = 1000 / this.ups;
        for (long past = this.currentTimeMillis(), present; this.running; past = present) {
            present = this.currentTimeMillis();
            final long duration = present - past;
            final long deadline = present + timeout;
            this.addJob(STEP_PILL);
            this.game.step(duration / 1000.0F);
            for (RunnableFuture<?> job; (job = this.pollJob()) != null && job != STEP_PILL; job.run());
            try {
                for (long now; (now = this.currentTimeMillis()) < deadline; ) {
                    this.selector.select(deadline - now);
                    final Set<SelectionKey> keys = this.selector.selectedKeys();
                    for (final Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); it.remove()) {
                        final SelectionKey key = it.next();
                        try {
                            if (key.isAcceptable()) {
                                this.accept(this.selector, socket, key);
                            } else {
                                if (key.isReadable()) {
                                    this.read(key);
                                }
                                if (key.isValid() && key.isWritable()) {
                                    this.write(key);
                                }
                            }
                        } catch (final IOException e) {
                            LOGGER.log(Level.WARNING, "Network error", e);
                            key.cancel();
                        }
                    }
                }
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Network error", e);
                this.running = false;
            }
        }
        this.lan.interrupt();
        try {
            this.lan.join();
        } catch (final InterruptedException ignored) {}
        try {
            socket.close();
        } catch (final IOException ignored) {}
        try {
            this.selector.close();
        } catch (final IOException ignored) {}
    }

    @Override
    public void send(final Packet packet) {
        for (final SelectionKey key : this.selector.keys()) {
            final Object att = key.attachment();
            if (att instanceof SimpleConnection) {
                ((SimpleConnection) att).send(packet);
            }
        }
    }

    private void accept(final Selector selector, final ServerSocketChannel server, final SelectionKey acceptKey) throws IOException {
        final SocketChannel socket = server.accept();
        socket.configureBlocking(false);
        final SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
        final SimpleConnection connection = new SimpleConnection(key, this.factory.create());
        connection.setContext(this.factory.create(new ServerContext(new Context(connection), this)));
        key.attach(connection);
        LOGGER.info("Accepting connection from " + socket.getRemoteAddress());
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

    public static SimpleServer open(final InetSocketAddress address, final GameModel game, final long ups) throws IOException {
        return new SimpleServer(
            Selector.open(),
            address,
            Protocol.createConnectionFactory(),
            game,
            new LinkedBlockingDeque<>(),
            ups,
            new Thread(LanAdvertiser.createPublisher(
                ServerAdvertisement.builder()
                    .setAddress(address)
                    .build()
            ))
        );
    }
}
