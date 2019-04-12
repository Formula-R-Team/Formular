package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;

import io.github.formular_team.formular.core.Game;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class AddRacePacket implements Packet {
    @Override
    public void write(final ByteBuffer buf) {

    }

    @Override
    public void read(final ByteBuffer buf) {

    }

    public static class Handler implements PacketHandler<Game> {
        @Override
        public void handle(final Game game) {

        }
    }
}
