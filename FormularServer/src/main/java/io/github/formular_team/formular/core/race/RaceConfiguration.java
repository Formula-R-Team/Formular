package io.github.formular_team.formular.core.race;

public final class RaceConfiguration{
    private final int lapCount;

    private final boolean timeTrial;

    private final int racerCap;

    private final boolean allowCpus;

    private final boolean allowSpectators;

    public int getLapCount() {
        return this.lapCount;
    }

    public boolean isTimeTrial(){return timeTrial;}

    public int getRacerCap(){return racerCap;}

    public boolean isAllowCpus(){return allowCpus;}

    public boolean isAllowSpectators(){return allowSpectators;}


    private RaceConfiguration(final Builder builder){
        this.lapCount = builder.lapCount;
        this.timeTrial = builder.timeTrial;
        this.racerCap = builder.racerCap;
        this.allowCpus = builder.allowCpus;
        this.allowSpectators = builder.allowSpectators;
    }

    public final static class Builder{
        private int lapCount;

        private boolean timeTrial;

        private int racerCap;

        private boolean allowCpus;

        private boolean allowSpectators;

        public Builder lapCount(final int lapCount){
            this.lapCount = lapCount;
            return this;
        }

        public Builder timeTrial(final boolean timeTrial){
            this.timeTrial = timeTrial;
            return this;
        }

        public Builder racerCap(final int racerCap){
            this.racerCap = racerCap;
            return this;
        }

        public Builder allowCpus(final boolean allowCpus){
            this.allowCpus = allowCpus;
            return this;
        }

        public Builder allowSpectators(final boolean allowSpectators){
            this.allowSpectators = allowSpectators;
            return this;
        }

        public RaceConfiguration build() {
            return new RaceConfiguration(this);
        }
    }

    //public static RaceConfiguration create(final int lapCount) {
    //    return new RaceConfiguration(lapCount);
    //}
}
