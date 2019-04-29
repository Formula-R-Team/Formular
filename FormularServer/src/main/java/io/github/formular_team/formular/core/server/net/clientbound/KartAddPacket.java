package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

// TODO: include initial pose
public class KartAddPacket implements Packet {
    public static final Function<ByteBuffer, KartAddPacket> CREATOR = KartAddPacket::new;

    private final int uniqueId;

    private final int color;

    public KartAddPacket(final Kart kart) {
        this.uniqueId = kart.getUniqueId();
        this.color = kart.getColor();
    }

    public KartAddPacket(final ByteBuffer buf) {
        this.uniqueId = buf.getInt();
        this.color = buf.getInt();
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        buf.putInt(this.uniqueId);
        buf.putInt(this.color);
    }

    public static class Handler implements PacketHandler<ClientContext, KartAddPacket, ClientContext> {
        @Override
        public ClientContext apply(final ClientContext context, final KartAddPacket packet) {
            final GameView game = context.getClient().getGame();
            game.createKart(packet.uniqueId, packet.color);
            return context;
        }
    }
}
