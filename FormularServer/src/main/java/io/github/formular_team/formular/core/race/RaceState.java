package io.github.formular_team.formular.core.race;

import io.github.formular_team.formular.core.math.Mth;

abstract class RaceState {
    final Race race;

    RaceState(final Race race) {
        this.race = race;
    }

    RaceState step(final float delta) {
        this.race.stepRacers(delta);
        return this;
    }

    static class Start extends RaceState {
        Start(final Race race) {
            super(race);
        }
    }

    static class Starting extends RaceState {
        private float countdown = 3.0F;

        Starting(final Race race) {
            super(race);
        }

        @Override
        RaceState step(final float delta) {
            RaceState ret = super.step(delta);
            if (this.countdown > -1.0F) {
                final int c = (int) Mth.ceil(this.countdown);
                this.countdown -= delta;
                if ((int) Mth.ceil(this.countdown) != c) {
                    this.race.onCount(c);
                }
                if (this.countdown < -1.0F) {
                    this.countdown = -1.0F;
                    ret = new Racing(this.race);
                    this.race.go();
                }
            }
            return ret;
        }
    }

    static class Racing extends RaceState {
        Racing(final Race race) {
            super(race);
        }
    }

    static class Finish extends RaceState {
        Finish(final Race race) {
            super(race);
        }
    }
}
