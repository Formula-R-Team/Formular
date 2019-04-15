package io.github.formular_team.formular;

import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class WeakOptional<T> {
    private static final WeakOptional<?> EMPTY = new WeakOptional<>();

    private final WeakReference<T> ref;

    private WeakOptional() {
        this(new WeakReference<>(null));
    }

    private WeakOptional(final WeakReference<T> ref) {
        this.ref = ref;
    }

    private T getValue() {
        return this.ref.get();
    }

    public boolean isPresent() {
        return this.getValue() != null;
    }

    public T get() {
        final T value = this.getValue();
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    public void ifPresent(final Consumer<? super T> consumer) {
        final T value = this.getValue();
        if (value != null) {
            consumer.accept(value);
        }
    }

    public T orElse(final T other) {
        final T value = this.getValue();
        return value != null ? value : other;
    }

    public <U> WeakOptional<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final T value = this.getValue();
        if (value != null) {
            return ofNullable(mapper.apply(value));
        }
        return empty();
    }

    public static <T> WeakOptional<T> empty() {
        @SuppressWarnings("unchecked")
        final WeakOptional<T> t = (WeakOptional<T>) EMPTY;
        return t;
    }

    public static <T> WeakOptional<T> of(final T value) {
        Objects.requireNonNull(value);
        return new WeakOptional<>(new WeakReference<>(value));
    }

    public static <T> WeakOptional<T> ofNullable(final T value) {
        return value != null ? of(value) : empty();
    }
}
