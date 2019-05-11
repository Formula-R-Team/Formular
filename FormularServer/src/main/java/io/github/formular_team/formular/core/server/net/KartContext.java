package io.github.formular_team.formular.core.server.net;

import java.util.Objects;

import io.github.formular_team.formular.core.kart.Kart;

public class KartContext extends UserContext {
    private final Kart kart;

    public KartContext(final UserContext parent, final Kart kart) {
        super(parent, parent.getUser());
        this.kart = Objects.requireNonNull(kart);
    }

    public Kart getKart() {
        return this.kart;
    }
}
