package io.github.formular_team.formular;

import io.github.formular_team.formular.server.NamespacedString;

public interface ViewController {
    void addKart(final int uniqueId, final NamespacedString definition);

    void removeKart(final int uniqueId);
}
