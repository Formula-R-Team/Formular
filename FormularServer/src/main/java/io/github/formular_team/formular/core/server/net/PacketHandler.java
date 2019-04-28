package io.github.formular_team.formular.core.server.net;

public interface PacketHandler<T extends Context, U extends Packet, R extends Context> {
    R apply(final T state, final U packet);
}
