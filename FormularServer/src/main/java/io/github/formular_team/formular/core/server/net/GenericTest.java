package io.github.formular_team.formular.core.server.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GenericTest {
    interface Builder {

    }

    static abstract class Node {}

    static class MapNode<K, V> {
        final MapNode<K, ? super V> parent;

        final Map<K, Consumer<V>> map;

        MapNode(final MapNode<K, ? super V> parent, final Map<K, Consumer<V>> map) {
            this.parent = parent;
            this.map = map;
        }

        Consumer<? super V> get(final K key) {
            final Consumer<V> v = this.map.get(key);
            return v == null ? this.parent.get(key) : v;
        }
    }

    static class Context {}

    static class FooContext extends Context {}

    static class FooBarContext extends FooContext {}

    static class Container<T extends Context> {
        T context;
        MapNode<String, T> node;
        void work(final String k) {
            this.node.get(k).accept(this.context);
        }
    }

    public static void node() {
        final MapNode<String, Context> root = new MapNode<>(null, new HashMap<>());
        root.map.put("root", (Context context) -> {});
        root.get("root").accept(new Context());
        root.get("root").accept(new FooContext());
        final MapNode<String, FooContext> foo = new MapNode<>(root, new HashMap<>());
        foo.map.put("foo", (FooContext context) -> {});
        foo.get("foo").accept(new FooContext());
        final HashMap<String, Consumer<FooBarContext>> map = new HashMap<>();
        map.put("foobar", (FooBarContext context) -> {});
        final MapNode<String, FooBarContext> foobar = new MapNode<>(foo, map);
        foobar.map.put("foobar", (FooBarContext context) -> {});
        foobar.get("foobar").accept(new FooBarContext());
        foo.get("foo").accept(new FooBarContext());
        foo.get("foo").accept(new FooContext());
    }
}
