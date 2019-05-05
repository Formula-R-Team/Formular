package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class SetCountPacket implements Packet {
    public static final Function<ByteBuffer, SetCountPacket> CREATOR = SetCountPacket::new;

    private final int count;

    public SetCountPacket(final int count) {
        this.count = count;
    }

    public SetCountPacket(final ByteBuffer buf) {
        this.count = ByteBuffers.getUnsigned(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putUnsigned(buf, this.count);
    }

    public static final class Handler implements PacketHandler<ClientContext, SetCountPacket> {
        @Override
        public Context apply(final ClientContext context, final SetCountPacket packet) {
            context.getClient().getGame().setCount(packet.count);
            return context;
        }
    }
}
