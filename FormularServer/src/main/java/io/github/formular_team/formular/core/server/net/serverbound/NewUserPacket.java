package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import io.github.formular_team.formular.core.game.GameModel;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;
import io.github.formular_team.formular.core.server.net.ServerContext;
import io.github.formular_team.formular.core.server.net.UserContext;
import io.github.formular_team.formular.core.server.net.clientbound.KartAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.RaceAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPosePacket;

public class NewUserPacket implements Packet {
    public static final Function<ByteBuffer, NewUserPacket> CREATOR = NewUserPacket::new;

    private final User user;

    public NewUserPacket(final User user) {
        this.user = Objects.requireNonNull(user);
    }

    public NewUserPacket(final ByteBuffer buf) {
        this.user = ByteBuffers.getUser(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putUser(buf, this.user);
    }

    private static final Logger LOGGER = Logger.getLogger("NewUserPacket");

    public static class Handler implements PacketHandler<ServerContext, Context, NewUserPacket> {
        @Override
        public UserContext apply(final ServerContext context, final NewUserPacket packet) {
            LOGGER.info("" + packet.user);
            // TODO: better management of sync
            final GameModel game = context.getServer().getGame();
            final Race race = game.getRace();
            if (race != null) {
                context.getRemote().send(new RaceAddPacket(race));
            }
            for (final KartModel kart : game.getKarts()) {
                context.getRemote().send(new KartAddPacket(kart));
                context.getRemote().send(new SetPosePacket(kart));
            }
            return new UserContext(context, packet.user);
        }
    }
}
