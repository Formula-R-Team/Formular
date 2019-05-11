package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ContextualPacketGraph {
    private final Map<Class<? extends Context>, NodeEntry<?>> entries;

    private final Map<Function<? super ByteBuffer, ?>, Integer> ids;

    private ContextualPacketGraph(final Builder builder) {
        this.entries = Collections.unmodifiableMap(builder.entries);
        this.ids = Collections.unmodifiableMap(builder.ids);
    }

    public ContextHolder<?> create() {
        return new ContextHolder<>(null, new RootNode<>());
    }

    public ContextHolder<?> create(final Context context) {
        return this.createState(context);
    }

    private <T extends Context> ContextHolder<T> createState(final T context) {
        return new ContextHolder<>(context, this.get(context));
    }

    @SuppressWarnings("unchecked")
    private <T extends Context> Node<T> get(final T context) {
        return (Node<T>) this.entries.get(context.getClass());
    }

    public static Builder builder() {
        return new Builder();
    }

    private interface Entry<T extends Context> {
        Function<T, Context> read(final ByteBuffer buf);
    }

    private static class PacketEntry<T extends Context, U extends Packet> implements Entry<T> {
        final Function<? super ByteBuffer, U> packet;

        final PacketHandler<? super T, ? super U> handler;

        PacketEntry(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, ? super U> handler) {
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
        <U extends Packet> NodeBuilder<T> accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, ? super U> handler);

        <S extends T> NodeBuilder<T> when(final Class<S> type, final Consumer<NodeBuilder<S>> consumer);
    }

    private static abstract class BaseBuilder<T extends Context> implements NodeBuilder<T> {
        final Class<T> type;

        final Map<Integer, Entry<T>> packets = new HashMap<>();

        final List<ChildBuilder<? extends T>> children = new ArrayList<>();

        private BaseBuilder(final Class<T> type) {
            this.type = type;
        }

        @Override
        public <U extends Packet> BaseBuilder<T> accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, ? super U> handler) {
            this.packets.put(this.assignId(packet), new PacketEntry<>(packet, handler));
            return this;
        }

        @Override
        public <S extends T> NodeBuilder<T> when(final Class<S> type, final Consumer<NodeBuilder<S>> consumer) {
            final ChildBuilder<S> child = new ChildBuilder<>(this, type);
            this.children.add(child);
            consumer.accept(child);
            return this;
        }

        abstract <E extends NodeEntry<?>> E add(final E node);

        abstract int assignId(final Function<? super ByteBuffer, ?> packet);

        NodeEntry<T> build(final Node<? super T> parent) {
            final NodeEntry<T> entry = new NodeEntry<>(parent, this.type, this.packets);
            for (final ChildBuilder<? extends T> child : this.children) {
                this.add(child.build(entry));
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
        public <U extends Packet> Builder accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<Context, ? super U> handler) {
            super.accept(packet, handler);
            return this;
        }

        @Override
        public <SUB extends Context> Builder when(final Class<SUB> type, final Consumer<NodeBuilder<SUB>> consumer) {
            super.when(type, consumer);
            return this;
        }

        public ContextualPacketGraph build() {
            this.add(this.build(new RootNode<>()));
            return new ContextualPacketGraph(this);
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
            return b -> t -> t;
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

    public final class ContextHolder<T extends Context> {
        private final T context;

        private final Node<T> node;

        ContextHolder(final T context, final Node<T> node) {
            this.context = context;
            this.node = node;
        }

        public void write(final ByteBuffer buf, final Packet packet) {
            final Integer type = ContextualPacketGraph.this.ids.get(packet.creator());
            if (type == null) {
                throw new RuntimeException("Undefined packet " + packet.getClass().getName());
            }
            ByteBuffers.putUnsignedShort(buf, type);
            ByteBuffers.putUnsignedShort(buf, 0);
            final int start = buf.position();
            packet.write(buf);
            final int end = buf.position();
            buf.position(start - Short.BYTES);
            final int length = end - start;
            if (length > (1 << Short.SIZE)) {
                throw new RuntimeException("Packet overflow: " + packet.getClass().getName() + ", length: " + length);
            }
            ByteBuffers.putUnsignedShort(buf, length);
            buf.position(end);
        }

        public Header<T> readHeader(final ByteBuffer buf) {
            final int id = ByteBuffers.getUnsignedShort(buf);
            final int length = ByteBuffers.getUnsignedShort(buf);
            return new Header<>(this.context, this.node.get(id), length);
        }
    }

    public final class Header<T extends Context> {
        private final T context;

        private final Entry<? super T> type;

        private final int length;

        private Header(final T context, final Entry<? super T> type, final int length) {
            this.context = context;
            this.type = type;
            this.length = length;
        }

        public int getLength() {
            return this.length;
        }

        public ContextHolder<?> readBody(final ByteBuffer buf) {
            return ContextualPacketGraph.this.createState(this.type.read(buf).apply(this.context));
        }
    }
}
