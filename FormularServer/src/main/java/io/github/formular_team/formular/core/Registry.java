package io.github.formular_team.formular.core;

public interface Registry<T> {
    T get(final NamespacedString name);

    interface Builder<T> {
        Builder<T> put(final NamespacedString name, final T value);

        Registry<T> build();
    }
}
