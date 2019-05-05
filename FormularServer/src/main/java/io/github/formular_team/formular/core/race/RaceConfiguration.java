package io.github.formular_team.formular.core.race;

public final class RaceConfiguration {
    private final int lapCount;

    private final boolean timeTrial;

    private final int racerCap;

    private final boolean cpus;

    private final boolean spectators;

    private RaceConfiguration(final Builder builder) {
        this.lapCount = builder.lapCount;
        this.timeTrial = builder.timeTrial;
        this.racerCap = builder.racerCap;
        this.cpus = builder.cpus;
        this.spectators = builder.spectators;
    }

    public int getLapCount() {
        return this.lapCount;
    }

    public boolean isTimeTrial() {
        return this.timeTrial;
    }

    public int getRacerCap() {
        return this.racerCap;
    }

    public boolean hasCpus() {
        return this.cpus;
    }

    public boolean hasSpectators() {
        return this.spectators;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private int lapCount;

        private boolean timeTrial;

        private int racerCap;

        private boolean cpus;

        private boolean spectators;

        private Builder() {
            this.lapCount = 3;
            this.timeTrial = false;
            this.racerCap = 0;
            this.cpus = false;
            this.spectators = true;
        }

        public Builder setLapCount(final int lapCount) {
            this.lapCount = lapCount;
            return this;
        }

        public Builder setTimeTrial(final boolean timeTrial) {
            this.timeTrial = timeTrial;
            return this;
        }

        public Builder setRacerCap(final int racerCap) {
            this.racerCap = racerCap;
            return this;
        }

        public Builder setCpus(final boolean cpus) {
            this.cpus = cpus;
            return this;
        }

        public Builder setSpectators(final boolean spectators) {
            this.spectators = spectators;
            return this;
        }

        public RaceConfiguration build() {
            return new RaceConfiguration(this);
        }
    }
}
