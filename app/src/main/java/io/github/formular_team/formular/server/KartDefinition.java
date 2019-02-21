package io.github.formular_team.formular.server;

public interface KartDefinition {
    String name();

    float size();

    float mass();

    float acceleration();

    interface Builder {
        Builder name(final String name);

        Builder size(final float size);

        Builder mass(final float mass);

        KartDefinition build();
    }
}
