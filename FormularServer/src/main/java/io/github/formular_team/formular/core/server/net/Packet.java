package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface Packet {
    Function<ByteBuffer, ? extends Packet> creator();

    void write(final ByteBuffer buf);
}
