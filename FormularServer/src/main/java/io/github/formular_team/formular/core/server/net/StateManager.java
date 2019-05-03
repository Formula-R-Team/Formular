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

    private final Map<Class<? extends Context>, NodeEntry<?>> entries;

    private final Map<Function<? super ByteBuffer, ?>, Integer> ids;

    private StateManager(final Builder builder) {
        this.entries = Collections.unmodifiableMap(builder.entries);
        this.ids = Collections.unmodifiableMap(builder.ids);
    }

    public ContextState<?> create() {
        return new ContextState<>(null, new RootNode<>());
    }

    public ContextState<?> create(final Context context) {
        return this.createState(context);
    }

    private <T extends Context> ContextState<T> createState(final T context) {
        //noinspection unchecked
        return new ContextState<>(context, (Node<T>) this.entries.get(context.getClass()));
    }

    public static Builder builder() {
        return new Builder();
    }

    private interface Entry<T extends Context> {
        Function<T, Context> read(final ByteBuffer buf);
    }

    private static class PacketEntry<T extends Context, U extends Packet> implements Entry<T> {
        final Function<? super ByteBuffer, U> packet;

        final PacketHandler<? super T, ? super U, ?> handler;

        PacketEntry(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U, ?> handler) {
            this.packet = packet;
            this.handler = handler;
        }

        @Override
        public Function<T, Context> read(final ByteBuffer buf) {
            final U u = this.packet.apply(buf);
            return t -> this.handler.apply(t, u);
        }
    }

    public interface NodeBuilder<T extends Context> {
        <U extends Packet> NodeBuilder<T> put(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, ? super U, ?> handler);

        <S extends T> NodeBuilder<T> in(final Class<S> type, final Consumer<NodeBuilder<S>> consumer);
    }

    private static abstract class BaseBuilder<T extends Context> implements NodeBuilder<T> {
        final Class<T> type;

        final Map<Integer, Entry<T>> packets = new HashMap<>();

        final List<ChildBuilder<? extends T>> subbuilders = new ArrayList<>();

        private BaseBuilder(final Class<T> type) {
            this.type = type;
        }

        @Override
        public <U extends Packet> BaseBuilder<T> put(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, ? super U, ?> handler) {
            this.packets.put(this.assignId(packet), new PacketEntry<>(packet, handler));
            return this;
        }

        @Override
        public <S extends T> NodeBuilder<T> in(final Class<S> type, final Consumer<NodeBuilder<S>> consumer) {
            final ChildBuilder<S> sub = new ChildBuilder<>(this, type);
            this.subbuilders.add(sub);
            consumer.accept(sub);
            return this;
        }

        abstract <E extends NodeEntry<?>> E add(final E node);

        abstract int assignId(final Function<? super ByteBuffer, ?> packet);

        NodeEntry<T> build(final Node<? super T> parent) {
            final NodeEntry<T> entry = new NodeEntry<>(parent, this.type, this.packets);
            for (final ChildBuilder<? extends T> sub : this.subbuilders) {
                this.add(sub.build(entry));
            }
            return entry;
        }
    }

    public static class Builder extends BaseBuilder<Context> {
        private final Map<Class<? extends Context>, NodeEntry<?>> entries = new HashMap<>();

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
        <E extends NodeEntry<?>> E add(final E node) {
            this.entries.put(node.type, node);
            return node;
        }

        @Override
        public <U extends Packet> Builder put(final Function<? super ByteBuffer, U> packet, final PacketHandler<Context, ? super U, ?> handler) {
            super.put(packet, handler);
            return this;
        }

        @Override
        public <SUB extends Context> Builder in(final Class<SUB> type, final Consumer<NodeBuilder<SUB>> consumer) {
            super.in(type, consumer);
            return this;
        }

        public StateManager build() {
            this.add(this.build(new RootNode<>()));
            return new StateManager(this);
        }
    }

    private static final class ChildBuilder<T extends Context> extends BaseBuilder<T> {
        final BaseBuilder<?> parent;

        private ChildBuilder(final BaseBuilder<?> parent, final Class<T> type) {
            super(type);
            this.parent = parent;
        }

        @Override
        int assignId(final Function<? super ByteBuffer, ?> packet) {
            return this.parent.assignId(packet);
        }

        @Override
        <E extends NodeEntry<?>> E add(final E node) {
            return this.parent.add(node);
        }
    }

    private interface Node<T extends Context> {
        Entry<? super T> get(final int id);
    }

    private static final class RootNode<T extends Context> implements Node<T> {
        @Override
        public Entry<? super T> get(final int id) {
            return buf -> t -> t;
        }
    }

    private static final class NodeEntry<T extends Context> implements Node<T> {
        final Node<? super T> parent;

        final Class<T> type;

        final Map<Integer, Entry<T>> packets;

        NodeEntry(final Node<? super T> parent, final Class<T> type, final Map<Integer, Entry<T>> packets) {
            this.parent = parent;
            this.type = type;
            this.packets = packets;
        }

        @Override
        public Entry<? super T> get(final int id) {
            final Entry<T> entry = this.packets.get(id);
            return entry == null ? this.parent.get(id) : entry;
        }
    }

    public final class ContextState<T extends Context> {
        private final T context;

        private final Node<T> node;

        ContextState(final T context, final Node<T> node) {
            this.context = context;
            this.node = node;
        }

        public void write(final ByteBuffer buf, final Packet packet) {
            final Integer type = StateManager.this.ids.get(packet.creator());
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

        public HeaderContext<T> readHeader(final ByteBuffer buf) {
            final int id = ByteBuffers.getUnsignedShort(buf);
            final int length = ByteBuffers.getUnsignedShort(buf);
            return new HeaderContext<>(this.context, this.node.get(id), length);
        }
    }

    public final class HeaderContext<T extends Context> {
        private final T context;

        private final Entry<? super T> type;

        private final int length;

        private HeaderContext(final T context, final Entry<? super T> type, final int length) {
            this.context = context;
            this.type = type;
            this.length = length;
        }

        public int getLength() {
            return this.length;
        }

        public ContextState<?> readBody(final ByteBuffer buf) {
            return StateManager.this.createState(this.type.read(buf).apply(this.context));
        }
    }
}
