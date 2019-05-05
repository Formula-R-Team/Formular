package io.github.formular_team.formular.core.server.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

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

    public static class ThreadLanServerFind extends Thread
    {
        private final InetAddress broadcastAddress;

        private final MulticastSocket socket;

        public ThreadLanServerFind() throws IOException
        {
            super("LanServerDetector #");
            this.setDaemon(true);
            this.socket = new MulticastSocket(4445);
            this.broadcastAddress = InetAddress.getByName("224.0.2.60");
            this.socket.setSoTimeout(5000);
            this.socket.joinGroup(this.broadcastAddress);
        }

        public void run()
        {
            final byte[] abyte = new byte[1024];

            while (!this.isInterrupted())
            {
                final DatagramPacket datagrampacket = new DatagramPacket(abyte, abyte.length);

                try
                {
                    this.socket.receive(datagrampacket);
                }
                catch (final SocketTimeoutException var5)
                {
                    continue;
                }
                catch (final IOException ioexception)
                {
                    break;
                }

                final String s = new String(datagrampacket.getData(), datagrampacket.getOffset(), datagrampacket.getLength(), StandardCharsets.UTF_8);
                datagrampacket.getAddress();
            }

            try
            {
                this.socket.leaveGroup(this.broadcastAddress);
            }
            catch (final IOException var4)
            {
                ;
            }

            this.socket.close();
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
