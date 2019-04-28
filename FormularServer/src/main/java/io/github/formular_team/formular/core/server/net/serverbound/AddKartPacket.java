package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.GameModel;
import io.github.formular_team.formular.core.KartModel;
import io.github.formular_team.formular.core.SimpleDriver;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.KartContext;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;
import io.github.formular_team.formular.core.server.net.UserContext;

public class AddKartPacket implements Packet {
    public static final Function<ByteBuffer, AddKartPacket> CREATOR = AddKartPacket::new;

    private final Vector2 position;

    public AddKartPacket(final Vector2 position) {
        this.position = Objects.requireNonNull(position);
    }

    public AddKartPacket(final ByteBuffer buf) {
        this.position = ByteBuffers.getVector2(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putVector2(buf, this.position);
    }

    public static class Handler implements PacketHandler<UserContext, AddKartPacket, KartContext> {
        @Override
        public KartContext apply(final UserContext context, final AddKartPacket packet) {
            final GameModel game = context.getServer().getGame();
            final KartModel kart = game.createKart();
            kart.setPosition(packet.position);
            final Driver driver = SimpleDriver.create(context.getUser(), kart);
            game.addKart(kart);
            game.addDriver(driver);
            return new KartContext(context, kart);
        }
    }
}
