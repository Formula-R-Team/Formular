package io.github.formular_team.formular.core.server.net;

public interface PacketHandler<T extends S, S, U extends Packet> {
    S apply(final T context, final U packet);
}
