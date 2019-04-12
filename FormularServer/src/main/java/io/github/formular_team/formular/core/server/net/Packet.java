package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;

public interface Packet {
    void write(final ByteBuffer buf);

    void read(final ByteBuffer buf);
}
