package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.Packet;

public class EnterRacePacket implements Packet {
    public static final Function<ByteBuffer, EnterRacePacket> CREATOR = EnterRacePacket::new;

    public EnterRacePacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}
}
