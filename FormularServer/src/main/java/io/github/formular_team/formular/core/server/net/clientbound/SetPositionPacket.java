package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class SetPositionPacket implements Packet {
    public static final Function<ByteBuffer, SetPositionPacket> CREATOR = SetPositionPacket::new;

    private final int position;

    public SetPositionPacket(final int position) {
        this.position = position;
    }

    public SetPositionPacket(final ByteBuffer buf) {
        this.position = ByteBuffers.getUnsigned(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putUnsigned(buf, this.position);
    }

    public static final class Handler implements PacketHandler<ClientContext, Context, SetPositionPacket> {
        @Override
        public Context apply(final ClientContext context, final SetPositionPacket packet) {
            context.getClient().getGame().setPosition(packet.position);
            return context;
        }
    }
}
