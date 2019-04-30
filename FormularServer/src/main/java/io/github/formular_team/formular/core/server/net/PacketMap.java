package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class PacketMap<T extends Context> {
    private final Map<Integer, PacketMap.Entry<T>> entries;

    private PacketMap(final PacketMap.Builder<T> builder) {
        this.entries = Collections.unmodifiableMap(builder.entries);
    }

    public Optional<Entry<? super T>> get(final int type) {
        return Optional.ofNullable(this.entries.get(type));
    }

    public static <T extends Context> PacketMap.Builder<T> builder() {
        return new PacketMap.Builder<>();
    }

    public static final class Header<T extends Context> {
        private final PacketMap.Entry<T> type;

        private final int length;

        Header(final PacketMap.Entry<T> type, final int length) {
            this.type = type;
            this.length = length;
        }

        public PacketMap.Entry<T> getType() {
            return this.type;
        }

        public int getLength() {
            return this.length;
        }
    }

    public interface Entry<T extends Context> {
        ContextFunction<T> read(final ByteBuffer buf);
    }

    public interface ContextFunction<T extends Context> extends Function<T, Context> {}

    private static class PacketEntry<T extends Context, U extends Packet> implements PacketMap.Entry<T> {
        final Function<? super ByteBuffer, U> packet;

        final PacketHandler<? super T, ? super U, ?> handler;

        PacketEntry(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U, ?> handler) {
            this.packet = packet;
            this.handler = handler;
        }

        @Override
        public ContextFunction<T> read(final ByteBuffer buf) {
            final U u = this.packet.apply(buf);
            return t -> this.handler.apply(t, u);
        }
    }

    public static final class Builder<T extends Context> {
        private final Map<Integer, PacketMap.Entry<T>> entries;

        private Builder() {
            this(new HashMap<>());
        }

        private Builder(final Map<Integer, PacketMap.Entry<T>> entries) {
            this.entries = entries;
        }

        public <U extends Packet> PacketMap.Builder<T> put(final int id, final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U, ?> handler) {
            this.entries.put(id, new PacketMap.PacketEntry<>(packet, handler));
            return this;
        }

        public PacketMap<T> build() {
            return new PacketMap<>(this);
        }
    }
}
