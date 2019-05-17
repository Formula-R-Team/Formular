package io.github.formular_team.formular.core.server.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

// https://stackoverflow.com/q/37293456/2782338
public final class LanAdvertiser {
    private static final String HOST = "224.0.2.17";

    private static final int PORT = 4687;

    private static final String MAGIC = "FORMULAR";

    public static Runnable createPublisher(final ServerAdvertisement advertisement) {
        final InetAddress address;
        try {
            address = InetAddress.getByName(HOST);
        } catch (final UnknownHostException e) {
            throw new InternalError(e);
        }
        return new Publisher(address, LanAdvertiser.createProtocol(a -> {}).create(new PublisherContext()), advertisement);
    }

    public static Runnable createSubscriber(final Consumer<ServerAdvertisement> consumer) {
        final InetAddress address;
        try {
            address = InetAddress.getByName(HOST);
        } catch (final UnknownHostException e) {
            throw new InternalError(e);
        }
        return new Subscriber(address, LanAdvertiser.createProtocol(consumer).create(new SubscriberContext()));
    }

    private static PacketGraph<Context> createProtocol(final Consumer<ServerAdvertisement> listener) {
        return PacketGraph.builder(Context.class)
            .when(ReceiveContext.class, receiver -> receiver
                .accept(AdvertisePacket.CREATOR, new AdvertisePacket.Handler(listener))
            )
            .build();

    }

    interface Context {}

    private static class PublisherContext implements Context {}

    private static class SubscriberContext implements Context {}

    private static class ReceiveContext extends SubscriberContext {
        private final InetAddress address;

        ReceiveContext(final InetAddress address) {
            this.address = address;
        }

        InetAddress getAddress() {
            return this.address;
        }
    }

    static class AdvertisePacket implements Packet {
        static Function<ByteBuffer, ? extends AdvertisePacket> CREATOR = AdvertisePacket::new;

        private final int port;

        AdvertisePacket(final ServerAdvertisement advertisement) {
            this.port = advertisement.getAddress().getPort();
        }

        AdvertisePacket(final ByteBuffer buf) {
            this.port = ByteBuffers.getUnsignedShort(buf);
        }

        @Override
        public Function<ByteBuffer, ? extends Packet> creator() {
            return CREATOR;
        }

        @Override
        public void write(final ByteBuffer buf) {
            ByteBuffers.putUnsignedShort(buf, this.port);
        }

        public static class Handler implements PacketHandler<ReceiveContext, Context, AdvertisePacket> {
            private final Consumer<ServerAdvertisement> listener;

            Handler(final Consumer<ServerAdvertisement> listener) {
                this.listener = listener;
            }

            @Override
            public Context apply(final ReceiveContext context, final AdvertisePacket packet) {
                this.listener.accept(ServerAdvertisement.builder()
                    .setAddress(new InetSocketAddress(context.getAddress(), packet.port))
                    .build());
                return context;
            }
        }
    }

    private static final int HEADER_LEN = 2 * Short.BYTES;

    private static final int PAYLOAD_MAX_LEN = 1 << Short.SIZE;

    private static final int PACKET_LEN = PAYLOAD_MAX_LEN + HEADER_LEN;

    static abstract class Endpoint implements Runnable {
        final InetAddress address;

        PacketGraph<Context>.ContextHolder<?> context;

        private Endpoint(final InetAddress address, final PacketGraph<Context>.ContextHolder<?> context) {
            this.address = address;
            this.context = context;
        }
    }

    static class Publisher extends Endpoint {
        private final ServerAdvertisement advertisement;

        private Publisher(final InetAddress address, final PacketGraph<Context>.ContextHolder<?> context, final ServerAdvertisement advertisement) {
            super(address, context);
            this.advertisement = advertisement;
        }

        @Override
        public void run() {
            final DatagramSocket socket;
            try {
                socket = new DatagramSocket();
            } catch (final SocketException e) {
                throw new RuntimeException(e);
            }
            final byte[] bytes = new byte[PACKET_LEN];
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.address, PORT);
            final ByteBuffer buf = ByteBuffer.wrap(bytes);
            ByteBuffers.putChars(buf, MAGIC);
            this.context.write(buf, new AdvertisePacket(this.advertisement));
            buf.flip();
            packet.setLength(buf.limit());
            while (true) {
                try {
                    Logger.getLogger("waldo").info("LAN publisher send");
                    socket.send(packet);
                } catch (final IOException e) {
                    Logger.getLogger("waldo").log(Level.SEVERE, "Send failure", e);
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    break;
                }
            }
            Logger.getLogger("waldo").info("LAN publisher stopped");
            socket.close();
        }
    }

    static class Subscriber extends Endpoint {
        private Subscriber(final InetAddress address, final PacketGraph<Context>.ContextHolder<?> context) {
            super(address, context);
        }

        @Override
        public void run() {
            final MulticastSocket socket;
            try {
                socket = new MulticastSocket(PORT);
                socket.setSoTimeout(5000);
                socket.joinGroup(this.address);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final byte[] bytes = new byte[PACKET_LEN];
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            final ByteBuffer buf = ByteBuffer.wrap(bytes);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket.receive(packet);
                } catch (final SocketTimeoutException e) {
                    continue;
                } catch (final IOException e) {
                    break;
                }
                buf.position(packet.getOffset());
                buf.limit(packet.getOffset() + packet.getLength());
                if (MAGIC.equals(ByteBuffers.getChars(buf, MAGIC.length()))) {
                    Logger.getLogger("waldo").info("LAN publisher receive");
                    this.context = this.context.create(new ReceiveContext(packet.getAddress()));
                    this.context = this.context.readHeader(buf).readBody(buf);
                } else {
                    Logger.getLogger("waldo").info("LAN publisher received magic mismatch");
                }
            }
            Logger.getLogger("waldo").info("LAN subscriber stopped");
            try {
                socket.leaveGroup(this.address);
            } catch (final IOException ignored) {}
            socket.close();
        }
    }
}
