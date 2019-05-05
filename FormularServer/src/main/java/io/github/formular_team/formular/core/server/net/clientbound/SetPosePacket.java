package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class SetPosePacket implements Packet {
    public static final Function<ByteBuffer, SetPosePacket> CREATOR = SetPosePacket::new;

    private final int uniqueId;

    private final Vector2 position;

    private final float rotation;

    public SetPosePacket(final Kart kart) {
        this.uniqueId = kart.getUniqueId();
        this.position = kart.getPosition();
        this.rotation = kart.getRotation();
    }

    public SetPosePacket(final ByteBuffer buf) {
        this.uniqueId = buf.getInt();
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
        ByteBuffers.putVector2(buf ,this.position);
        buf.putFloat(this.rotation);
    }

    public static class Handler implements PacketHandler<ClientContext, SetPosePacket> {
        @Override
        public ClientContext apply(final ClientContext context, final SetPosePacket packet) {
            final GameView game = context.getClient().getGame();
            game.getKart(packet.uniqueId)
                .ifPresent(kart -> {
                    kart.setPosition(packet.position);
                    kart.setRotation(packet.rotation);
                });
            return context;
        }
    }
}
