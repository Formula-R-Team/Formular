package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class RaceFinishPacket implements Packet {
    public static final Function<ByteBuffer, RaceFinishPacket> CREATOR = RaceFinishPacket::new;

    public RaceFinishPacket() {}

    public RaceFinishPacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}

    public static final class Handler implements PacketHandler<ClientContext, Context, RaceFinishPacket> {
        @Override
        public Context apply(final ClientContext context, final RaceFinishPacket packet) {
            context.getClient().getGame().onFinish();
            return context;
        }
    }
}
