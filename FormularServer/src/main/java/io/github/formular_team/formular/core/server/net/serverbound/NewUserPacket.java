package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;
import io.github.formular_team.formular.core.server.net.ServerContext;
import io.github.formular_team.formular.core.server.net.UserContext;

public class NewUserPacket implements Packet {
    public static final Function<ByteBuffer, NewUserPacket> CREATOR = NewUserPacket::new;

    private final User user;

    public NewUserPacket(final User user) {
        this.user = Objects.requireNonNull(user);
    }

    public NewUserPacket(final ByteBuffer buf) {
        final String name = ByteBuffers.getString(buf);
        final int color = buf.getInt();
        this.user = User.create(name, color);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putString(buf, this.user.getName());
        buf.putInt(this.user.getColor());
    }

    private static final Logger LOGGER = Logger.getLogger(NewUserPacket.class.getName());

    public static class Handler implements PacketHandler<ServerContext, NewUserPacket, UserContext> {
        @Override
        public UserContext apply(final ServerContext context, final NewUserPacket packet) {
            LOGGER.info("" + packet.user);
            return new UserContext(context, packet.user);
        }
    }
}
