package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class SetLapPacket implements Packet {
    public static final Function<ByteBuffer, SetLapPacket> CREATOR = SetLapPacket::new;

    private final int lap;

    public SetLapPacket(final int lap) {
        this.lap = lap;
    }

    public SetLapPacket(final ByteBuffer buf) {
        this.lap = ByteBuffers.getUnsigned(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putUnsigned(buf, this.lap);
    }

    public static final class Handler implements PacketHandler<ClientContext, Context, SetLapPacket> {
        @Override
        public Context apply(final ClientContext context, final SetLapPacket packet) {
            context.getClient().getGame().setLap(packet.lap);
            return context;
        }
    }
}
