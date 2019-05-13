package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PacketGraph<S> {
    private final Map<Class<? extends S>, NodeEntry<?, S>> entries;

    private final Map<Function<? super ByteBuffer, ?>, Integer> ids;

    private PacketGraph(final Builder<S> builder) {
        this.entries = Collections.unmodifiableMap(builder.entries);
        this.ids = Collections.unmodifiableMap(builder.ids);
    }

    public ContextHolder<?> create() {
        return new ContextHolder<>(null, new RootNode<>());
    }

    public ContextHolder<?> create(final S context) {
        return this.createState(context);
    }

    private <T extends S> ContextHolder<T> createState(final T context) {
        return new ContextHolder<>(context, this.get(context));
    }

    @SuppressWarnings("unchecked")
    private <T extends S> Node<T, S> get(final T context) {
        //noinspection SuspiciousMethodCalls
        return (Node<T, S>) this.entries.get(context.getClass());
    }

    public static <S> Builder<S> builder(final Class<S> type) {
        return new Builder<>(type);
    }

    private interface Entry<T extends S, S> {
        Function<T, S> read(final ByteBuffer buf);
    }

    private static class PacketEntry<T extends S, S, U extends Packet> implements Entry<T, S> {
        final Function<? super ByteBuffer, U> packet;

        final PacketHandler<? super T, S, ? super U> handler;

        PacketEntry(final Function<? super ByteBuffer, U> packet, final PacketHandler<? super T, S, ? super U> handler) {
            this.packet = packet;
            this.handler = handler;
        }

        @Override
        public Function<T, S> read(final ByteBuffer buf) {
            final U u = this.packet.apply(buf);
            return t -> this.handler.apply(t, u);
        }
    }

    public interface NodeBuilder<T extends S, S> {
        <U extends Packet> NodeBuilder<T, S> accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, S, ? super U> handler);

        <E extends T> NodeBuilder<T, S> when(final Class<E> type, final Consumer<NodeBuilder<E, S>> consumer);
    }

    private static abstract class BaseBuilder<T extends S, S> implements NodeBuilder<T, S> {
        final Class<T> type;

        final Map<Integer, Entry<T, S>> packets = new HashMap<>();

        final List<ChildBuilder<? extends T, S>> children = new ArrayList<>();

        private BaseBuilder(final Class<T> type) {
            this.type = type;
        }

        @Override
        public <U extends Packet> BaseBuilder<T, S> accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<T, S, ? super U> handler) {
            this.packets.put(this.assignId(packet), new PacketEntry<>(packet, handler));
            return this;
        }

        @Override
        public <E extends T> NodeBuilder<T, S> when(final Class<E> type, final Consumer<NodeBuilder<E, S>> consumer) {
            final ChildBuilder<E, S> child = new ChildBuilder<>(this, type);
            this.children.add(child);
            consumer.accept(child);
            return this;
        }

        abstract <E extends NodeEntry<?, S>> E add(final E node);

        abstract int assignId(final Function<? super ByteBuffer, ?> packet);

        NodeEntry<T, S> build(final Node<? super T, S> parent) {
            final NodeEntry<T, S> entry = new NodeEntry<>(parent, this.type, this.packets);
            for (final ChildBuilder<? extends T, S> child : this.children) {
                this.add(child.build(entry));
            }
            return entry;
        }
    }

    public static class Builder<S> extends BaseBuilder<S, S> {
        private final Map<Class<? extends S>, NodeEntry<?, S>> entries = new HashMap<>();

        private final Map<Function<? super ByteBuffer, ?>, Integer> ids = new HashMap<>();

        private int nextId;

        private Builder(final Class<S> type) {
            super(type);
        }

        @Override
        int assignId(final Function<? super ByteBuffer, ?> packet) {
            final int id = this.nextId++;
            this.ids.put(packet, id);
            return id;
        }

        @Override
        <E extends NodeEntry<?, S>> E add(final E node) {
            this.entries.put(node.type, node);
            return node;
        }

        @Override
        public <U extends Packet> Builder<S> accept(final Function<? super ByteBuffer, U> packet, final PacketHandler<S, S, ? super U> handler) {
            super.accept(packet, handler);
            return this;
        }

        @Override
        public <E extends S> Builder<S> when(final Class<E> type, final Consumer<NodeBuilder<E, S>> consumer) {
            super.when(type, consumer);
            return this;
        }

        public PacketGraph<S> build() {
            this.add(this.build(new RootNode<>()));
            return new PacketGraph<>(this);
        }
    }

    private static final class ChildBuilder<T extends S, S> extends BaseBuilder<T, S> {
        final BaseBuilder<?, S> parent;

        private ChildBuilder(final BaseBuilder<?, S> parent, final Class<T> type) {
            super(type);
            this.parent = parent;
        }

        @Override
        int assignId(final Function<? super ByteBuffer, ?> packet) {
            return this.parent.assignId(packet);
        }

        @Override
        <E extends NodeEntry<?, S>> E add(final E node) {
            return this.parent.add(node);
        }
    }

    private interface Node<T extends S, S> {
        Entry<? super T, S> get(final int id);
    }

    private static final class RootNode<T extends S, S> implements Node<T, S> {
        @Override
        public Entry<? super T, S> get(final int id) {
            return b -> t -> t;
        }
    }

    private static final class NodeEntry<T extends S, S> implements Node<T, S> {
        final Node<? super T, S> parent;

        final Class<T> type;

        final Map<Integer, Entry<T, S>> packets;

        NodeEntry(final Node<? super T, S> parent, final Class<T> type, final Map<Integer, Entry<T, S>> packets) {
            this.parent = parent;
            this.type = type;
            this.packets = packets;
        }

        @Override
        public Entry<? super T, S> get(final int id) {
            final Entry<T, S> entry = this.packets.get(id);
            return entry == null ? this.parent.get(id) : entry;
        }
    }

    public final class ContextHolder<T extends S> {
        private final T context;

        private final Node<T, S> node;

        ContextHolder(final T context, final Node<T, S> node) {
            this.context = context;
            this.node = node;
        }

        public void write(final ByteBuffer buf, final Packet packet) {
            final Integer type = PacketGraph.this.ids.get(packet.creator());
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

    public final class Header<T extends S> {
        private final T context;

        private final Entry<? super T, S> type;

        private final int length;

        private Header(final T context, final Entry<? super T, S> type, final int length) {
            this.context = context;
            this.type = type;
            this.length = length;
        }

        public int getLength() {
            return this.length;
        }

        public PacketGraph<S>.ContextHolder<?> readBody(final ByteBuffer buf) {
            return PacketGraph.this.createState(this.type.read(buf).apply(this.context));
        }
    }
}
