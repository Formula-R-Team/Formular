package io.github.formular_team.formular.core.server.net;

public interface PacketHandler<T extends Context, U extends Packet> {
    Context apply(final T context, final U packet);
}
