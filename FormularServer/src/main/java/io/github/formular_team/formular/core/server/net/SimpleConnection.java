package io.github.formular_team.formular.core.server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleConnection implements Connection {
    private static final Logger LOGGER = Logger.getLogger("Connection");

    private static final int END_OF_STREAM = -1;

    private static final int HEADER_LEN = 2 * Short.BYTES;

    private static final int PAYLOAD_MAX_LEN = 1 << Short.SIZE;

    private static final int PACKET_LEN = PAYLOAD_MAX_LEN + HEADER_LEN;

    private final ByteBuffer writeBuf = ByteBuffer.allocateDirect(2 * PACKET_LEN);

    private final ByteBuffer readBuf = ByteBuffer.allocateDirect(PAYLOAD_MAX_LEN);

    private final SelectionKey key;

    private StateManager.ContextState<?> context;

    private ReadState readState;

    private final Deque<Packet> backlog = new ArrayDeque<>();

    public SimpleConnection(final SelectionKey key, final StateManager.ContextState<?> context) {
        this.key = key;
        this.context = context;
        this.writeBuf.position(this.writeBuf.limit());
        this.readState(new HeaderReadState());
    }

    public void setContext(final StateManager.ContextState<?> context) {
        this.context = context;
    }

    private void readState(final ReadState state) {
        this.readState = state;
        this.readState.init(this.readBuf);
    }

    // TODO: not heuristic packet length, i.e. double buffer write
    private boolean hasAvailable() {
        return this.writeBuf.capacity() - this.writeBuf.limit() >= PACKET_LEN;
    }

    @Override
    public void send(final Packet packet) {
        if (!this.backlog.isEmpty()) {
            this.backlog.addLast(packet);
        } else if (this.writeBuf.hasRemaining()) {
            if (!this.hasAvailable()) {
                this.writeBuf.compact();
                this.writeBuf.flip();
                if (!this.hasAvailable()) {
                    this.backlog.addLast(packet);
                } else {
                    this.append(packet);
                }
            } else {
                this.append(packet);
            }
        } else {
            this.writeBuf.clear();
            this.context.write(this.writeBuf, packet);
            this.writeBuf.flip();
        }
        this.key.interestOps(this.key.interestOps() | SelectionKey.OP_WRITE);
    }

    @Override
    public void close() {
        try {
            this.key.channel().close();
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error closing", e);
        }
        this.key.cancel();
    }

    private void append(final Packet packet) {
        this.writeBuf.mark();
        this.writeBuf.position(this.writeBuf.limit());
        this.writeBuf.limit(this.writeBuf.capacity());
        this.context.write(this.writeBuf, packet);
        this.writeBuf.limit(this.writeBuf.position());
        this.writeBuf.reset();
    }

    public void write(final SocketChannel socket, final SelectionKey key) throws IOException {
        socket.write(this.writeBuf);
        if (!this.backlog.isEmpty()) {
            this.writeBuf.compact();
            if (this.hasAvailable()) {
                this.append(this.backlog.removeFirst());
            }
        }
        if (this.writeBuf.hasRemaining()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }
    }

    public void read(final SocketChannel socket, final SelectionKey key) throws IOException {
        final int read = socket.read(this.readBuf);
        this.readState(this.readState.read(this.readBuf));
        if (read == END_OF_STREAM) {
            LOGGER.info("End of stream for " + socket.getRemoteAddress());
            socket.close();
            key.cancel();
        } else {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        }
    }

    private abstract class ReadState {
        final int length;

        ReadState(final int length) {
            this.length = length;
        }

        void init(final ByteBuffer buf) {
            final int newLimit = buf.position() + this.length;
            if (newLimit > buf.capacity()) {
                buf.clear();
                buf.mark();
                buf.limit(this.length);
            } else {
                buf.mark();
                buf.limit(newLimit);
            }
        }

        ReadState read(final ByteBuffer buf) {
            if (buf.hasRemaining()) {
                return this;
            }
            buf.reset();
            return this.complete(buf);
        }

        abstract ReadState complete(final ByteBuffer buf);
    }

    private final class HeaderReadState extends ReadState {
        HeaderReadState() {
            super(HEADER_LEN);
        }

        @Override
        ReadState complete(final ByteBuffer buf) {
            return new PayloadReadState(SimpleConnection.this.context.readHeader(buf));
        }
    }

    private final class PayloadReadState extends ReadState {
        final StateManager.HeaderContext header;

        PayloadReadState(final StateManager.HeaderContext header) {
            super(header.getLength());
            this.header = header;
        }

        @Override
        ReadState complete(final ByteBuffer buf) {
            SimpleConnection.this.context = this.header.readBody(buf);
            return new HeaderReadState();
        }
    }
}
