package io.github.formular_team.formular.server;

public final class RaceConfiguration {
    private final int lapCount;

    private final int positionCount;

    private RaceConfiguration(final int lapCount, final int positionCount) {
        this.lapCount = lapCount;
        this.positionCount = positionCount;
    }

    public int getLapCount() {
        return this.lapCount;
    }

    public int getPositionCount() {
        return this.positionCount;
    }

    public static RaceConfiguration create(final int lapCount, final int positionCount) {
        return new RaceConfiguration(lapCount, positionCount);
    }
}
