package io.github.formular_team.formular.core.server.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.function.Function;

// https://stackoverflow.com/q/37293456/2782338
public class LanDiscovery {
    private static final String INTERFACE = "eth0";

    private static final String HOST = "224.0.2.17";

    private static final int PORT = 4687;

    /*public void send() throws IOException  {
        final NetworkInterface ni = NetworkInterface.getByName(INTERFACE);
        final DatagramChannel dc = DatagramChannel.open()
            .bind(null)
            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        final ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        // write
        final InetSocketAddress address = new InetSocketAddress(HOST, PORT);
        dc.send(buf, address);
    }

    private void receive() throws IOException {
        final NetworkInterface ni = NetworkInterface.getByName(INTERFACE);
        final DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(PORT))
            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        final InetAddress group = InetAddress.getByName(HOST);
        final MembershipKey key = dc.join(group, ni);
        final ByteBuffer buf = ByteBuffer.allocate(1024);
        dc.receive(buf);
        buf.flip();
        // read
        key.drop();
    }*/

    static final String MAGIC = "FORMULAR";

    static PacketGraph<?> protocol() {
        return PacketGraph.builder(Context.class)
            /*.accept()*/
            .build();
    }

    interface Context {}

    interface SenderContext extends Context {}

    interface ReceiverContext extends Context {}

    static class ServerBroadcastPacket implements Packet {
        static Function<ByteBuffer, ? extends ServerBroadcastPacket> CREATOR = ServerBroadcastPacket::new;

        ServerBroadcastPacket(final ByteBuffer buf) {

        }

        @Override
        public Function<ByteBuffer, ? extends Packet> creator() {
            return CREATOR;
        }

        @Override
        public void write(final ByteBuffer buf) {

        }
    }

    static class Sender implements Runnable {
        @Override
        public void run() {

        }
    }

    static class Receiver implements Runnable {
        private static final int HEADER_LEN = 2 * Short.BYTES;

        private static final int PAYLOAD_MAX_LEN = 1 << Short.SIZE;

        private static final int PACKET_LEN = PAYLOAD_MAX_LEN + HEADER_LEN;

        private final InetAddress address;

        private final MulticastSocket socket;

        private PacketGraph<?>.ContextHolder<?> context;

        private Receiver(final InetAddress address, final MulticastSocket socket, final PacketGraph<?>.ContextHolder<?> context) {
            this.address = address;
            this.socket = socket;
            this.context = context;
        }

        @Override
        public void run() {
            final byte[] bytes = new byte[PACKET_LEN];
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            final ByteBuffer buf = ByteBuffer.wrap(bytes);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    this.socket.receive(packet);
                } catch (final SocketTimeoutException e) {
                    continue;
                } catch (final IOException e) {
                    break;
                }
                buf.position(packet.getOffset());
                buf.limit(packet.getOffset() + packet.getLength());
                if (MAGIC.equals(ByteBuffers.getChars(buf, MAGIC.length()))) {
                    this.context = this.context.readHeader(buf).readBody(buf);
                }
            }
            try {
                this.socket.leaveGroup(this.address);
            } catch (final IOException ignored) {}
            this.socket.close();
        }

        static Receiver create() throws IOException {
            final InetAddress address = InetAddress.getByName(HOST);
            final MulticastSocket socket = new MulticastSocket(PORT);
            socket.setSoTimeout(5000);
            socket.joinGroup(address);
            return new Receiver(address, socket, protocol().create());
        }
    }

    public static void main(final String[] args) throws IOException {
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            final NetworkInterface ni = interfaces.nextElement();
            try {
                if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast())
                    System.out.println("waldo " + ni);
            } catch (final SocketException ignored) {}
        }
    }
}
