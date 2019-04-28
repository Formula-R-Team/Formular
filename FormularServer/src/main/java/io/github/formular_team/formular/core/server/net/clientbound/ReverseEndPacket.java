package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.Packet;

public class ReverseEndPacket implements Packet {
    public static final Function<ByteBuffer, ReverseEndPacket> CREATOR = ReverseEndPacket::new;

    public ReverseEndPacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}
}
