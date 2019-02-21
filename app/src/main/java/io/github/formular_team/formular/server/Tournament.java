package io.github.formular_team.formular.server;

public interface Tournament {
    // TimeTrial, Versus
    interface Mode {

    }

    interface Builder {
        Builder mode(final Mode mode);

        Builder cap(final int cap);
    }
}
