package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.server.net.Packet;

public class SetTimePacket implements Packet {
    public static final Function<ByteBuffer, SetTimePacket> CREATOR = SetTimePacket::new;

    public SetTimePacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}
}
