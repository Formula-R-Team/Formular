package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public final class StateManager {
    private static final Logger LOGGER = Logger.getLogger("StateManager");

    private final Map<Class<? extends Context>, Entry<?>> entries;

    private final Map<Function<? super ByteBuffer, ?>, Integer> ids;

    private StateManager(final Builder builder) {
        this.entries = Collections.unmodifiableMap(builder.entries);
        this.ids = Collections.unmodifiableMap(builder.ids);
    }

    public StateManager.ContextState<?> create(final Context context) {
        return this.createState(context);
    }

    private <T extends Context> ContextState<T> createState(final T context) {
        //noinspection unchecked
        return new ContextState<>(context, (Entry<T>) this.entries.get(context.getClass()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface NodeBuilder<T extends Context> {
        <U extends Packet> NodeBuilder<T> put(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U, ?> handler);

        <SUB extends T> NodeBuilder<T> in(final Class<SUB> type, final Consumer<NodeBuilder<SUB>> consumer);
    }

    private static abstract class BaseBuilder<T extends Context> implements NodeBuilder<T> {
        final Class<T> type;

        final PacketMap.Builder<? super Context> packets = PacketMap.builder();

        final List<ChildBuilder<? extends T, T>> subbuilders = new ArrayList<>();

        private BaseBuilder(final Class<T> type) {
            this.type = type;
        }

        @Override
        public <U extends Packet> BaseBuilder<T> put(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U, ?> handler) {
            //noinspection unchecked FIXME
            this.packets.put(this.assignId(packet), packet, (PacketHandler<? super Context, ? super U, ?>) handler);
            return this;
        }

        @Override
        public <SUB extends T> NodeBuilder<T> in(final Class<SUB> type, final Consumer<NodeBuilder<SUB>> consumer) {
            final ChildBuilder<SUB, T> sub = new ChildBuilder<>(this, type);
            this.subbuilders.add(sub);
            consumer.accept(sub);
            return this;
        }

        abstract <E extends Entry<?>> E add(final E node);

        abstract int assignId(final Function<? super ByteBuffer, ?> packet);

        Entry<T> build(final Node parent) {
            final Entry<T> entry = new Entry<>(parent, this.type, this.packets.build());
            for (final ChildBuilder<? extends T, T> sub : this.subbuilders) {
                this.add(sub.build(entry));
            }
            return entry;
        }
    }

    public static class Builder extends BaseBuilder<Context> {
        private final Map<Class<? extends Context>, Entry<?>> entries = new HashMap<>();

        private final Map<Function<? super ByteBuffer, ?>, Integer> ids = new HashMap<>();

        private int nextId;

        private Builder() {
            super(Context.class);
        }

        @Override
        int assignId(final Function<? super ByteBuffer, ?> packet) {
            final int id = this.nextId++;
            this.ids.put(packet, id);
            return id;
        }

        @Override
        <E extends Entry<?>> E add(final E node) {
            this.entries.put(node.type, node);
            return node;
        }

        @Override
        public <U extends Packet> Builder put(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super Context, ? super U, ?> handler) {
            super.put(packet, handler);
            return this;
        }

        @Override
        public <SUB extends Context> Builder in(final Class<SUB> type, final Consumer<NodeBuilder<SUB>> consumer) {
            super.in(type, consumer);
            return this;
        }

        public StateManager build() {
            this.add(this.build(new RootNode()));
            return new StateManager(this);
        }
    }

    private static final class ChildBuilder<T extends SUP, SUP extends Context> extends BaseBuilder<T> {
        final BaseBuilder<SUP> parent;

        private ChildBuilder(final BaseBuilder<SUP> parent, final Class<T> type) {
            super(type);
            this.parent = parent;
        }

        @Override
        int assignId(final Function<? super ByteBuffer, ?> packet) {
            return this.parent.assignId(packet);
        }

        @Override
        <E extends Entry<?>> E add(final E node) {
            return this.parent.add(node);
        }
    }

    interface Node {
        PacketMap.Entry<? super Context> get(final int id);
    }

    static class RootNode implements Node {
        @Override
        public PacketMap.Entry<? super Context> get(final int id) {
            return buf -> t -> t;
        }
    }

    private final static class Entry<T extends Context> implements Node {
        final Node parent;

        final Class<T> type;

        final PacketMap<? super Context> packets;

        Entry(final Node parent, final Class<T> type, final PacketMap<? super Context> packets) {
            this.parent = parent;
            this.type = type;
            this.packets = packets;
        }

        @Override
        public PacketMap.Entry<? super Context> get(final int id) {
            return this.packets.get(id).orElseGet(() -> this.parent.get(id));
        }

        PacketMap.Header<? super Context> readHeader(final ByteBuffer buf) {
            final int id = ByteBuffers.getUnsignedShort(buf);
            final int length = ByteBuffers.getUnsignedShort(buf);
            return new PacketMap.Header<>(this.get(id), length);
        }
    }

    private void write(final ByteBuffer buf, final Packet packet) {
        final Integer type = this.ids.get(packet.creator());
        if (type != null) {
            ByteBuffers.putUnsignedShort(buf, type);
            ByteBuffers.putUnsignedShort(buf, 0);
            final int start = buf.position();
            packet.write(buf);
            final int end = buf.position();
            buf.position(start - Short.BYTES);
            ByteBuffers.putUnsignedShort(buf, end - start);
            buf.position(end);
        } else {
            LOGGER.warning("Packet id unknown for write: " + packet.getClass().getName());
        }
    }

    public final class ContextState<T extends Context> {
        private final T context;

        private final Entry<T> entry;

        ContextState(final T context, final Entry<T> entry) {
            this.context = context;
            this.entry = entry;
        }

        public void write(final ByteBuffer buf, final Packet packet) {
            StateManager.this.write(buf, packet);
        }

        public HeaderContext<T> readHeader(final ByteBuffer buf) {
            return new HeaderContext<>(this.context, this.entry.readHeader(buf));
        }
    }

    public final class HeaderContext<T extends Context> {
        private final T context;

        private final PacketMap.Header<? super Context> header;

        private HeaderContext(final T context, final PacketMap.Header<? super Context> header) {
            this.context = context;
            this.header = header;
        }

        public int getLength() {
            return this.header.getLength();
        }

        public ContextState<?> readBody(final ByteBuffer buf) {
            return StateManager.this.createState(this.header.getType().read(buf).apply(this.context));
        }
    }
}
