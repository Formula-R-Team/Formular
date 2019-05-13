package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.github.formular_team.formular.core.RaceFinishEntry;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class RaceResultsPacket implements Packet {
    public static final Function<ByteBuffer, RaceResultsPacket> CREATOR = RaceResultsPacket::new;

    private final List<RaceFinishEntry> entries;

    public RaceResultsPacket(final List<RaceFinishEntry> entries) {
        this.entries = Objects.requireNonNull(entries);
    }

    public RaceResultsPacket(final ByteBuffer buf) {
        final int count = ByteBuffers.getUnsigned(buf);
        this.entries = new ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            final User user = ByteBuffers.getUser(buf);
            final long time = buf.getLong();
            this.entries.add(new RaceFinishEntry(user, time));
        }
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putUnsigned(buf, this.entries.size());
        for (final RaceFinishEntry entry : this.entries) {
            ByteBuffers.putUser(buf, entry.getUser());
            buf.putLong(entry.getTime());
        }
    }

    public static final class Handler implements PacketHandler<ClientContext, Context, RaceResultsPacket> {
        @Override
        public Context apply(final ClientContext context, final RaceResultsPacket packet) {
            return context;
        }
    }
}
