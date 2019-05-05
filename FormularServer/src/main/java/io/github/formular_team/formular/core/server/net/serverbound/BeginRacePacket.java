package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.GameModel;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;
import io.github.formular_team.formular.core.server.net.UserContext;

public class BeginRacePacket implements Packet {
    public static final Function<ByteBuffer, BeginRacePacket> CREATOR = BeginRacePacket::new;

    public BeginRacePacket() {}

    public BeginRacePacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}

    public static class Handler implements PacketHandler<UserContext, BeginRacePacket> {
        @Override
        public Context apply(final UserContext context, final BeginRacePacket packet) {
            final GameModel game = context.getServer().getGame();
            final Race race = game.getRace();
            if (race != null) {
                race.start();
            }
            return context;
        }
    }
}
