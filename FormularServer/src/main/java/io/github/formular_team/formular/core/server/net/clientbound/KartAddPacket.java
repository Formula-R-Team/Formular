package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.game.GameView;
import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class KartAddPacket implements Packet {
    public static final Function<ByteBuffer, KartAddPacket> CREATOR = KartAddPacket::new;

    private final int uniqueId;

    private final Color color;

    private final Vector2 position;

    private final float rotation;

    public KartAddPacket(final Kart kart) {
        this.uniqueId = kart.getUniqueId();
        this.color = kart.getColor();
        this.position = kart.getPosition();
        this.rotation = kart.getRotation();
    }

    public KartAddPacket(final ByteBuffer buf) {
        this.uniqueId = buf.getInt();
        this.color = ByteBuffers.getColor(buf);
        this.position = ByteBuffers.getVector2(buf);
        this.rotation = buf.getFloat();
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        buf.putInt(this.uniqueId);
        ByteBuffers.putColor(buf, this.color);
        ByteBuffers.putVector2(buf, this.position);
        buf.putFloat(this.rotation);
    }

    public static class Handler implements PacketHandler<ClientContext, KartAddPacket> {
        @Override
        public ClientContext apply(final ClientContext context, final KartAddPacket packet) {
            final GameView game = context.getClient().getGame();
            game.createKart(packet.uniqueId, packet.color, packet.position, packet.rotation);
            return context;
        }
    }
}
