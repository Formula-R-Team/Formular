package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.Packet;

public class BeginRacePacket implements Packet {
    public static final Function<ByteBuffer, BeginRacePacket> CREATOR = BeginRacePacket::new;

    public BeginRacePacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}
}
