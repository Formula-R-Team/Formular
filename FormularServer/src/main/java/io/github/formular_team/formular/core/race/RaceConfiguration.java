package io.github.formular_team.formular.core.race;

public final class RaceConfiguration {
    private final int lapCount;

    private RaceConfiguration(final int lapCount) {
        this.lapCount = lapCount;
    }

    public int getLapCount() {
        return this.lapCount;
    }

    public static RaceConfiguration create(final int lapCount) {
        return new RaceConfiguration(lapCount);
    }
}
