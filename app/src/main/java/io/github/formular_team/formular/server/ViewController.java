package io.github.formular_team.formular.server;

public interface ViewController {
    void addKart(final int uniqueId, final NamespacedString definition);

    void removeKart(final int uniqueId);
}
