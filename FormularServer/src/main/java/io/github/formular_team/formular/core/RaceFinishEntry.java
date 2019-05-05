package io.github.formular_team.formular.core;

public final class RaceFinishEntry {
    private final User user;

    private final long time;

    public RaceFinishEntry(final User user, final long time) {
        this.user = user;
        this.time = time;
    }

    public User getUser() {
        return this.user;
    }

    public long getTime() {
        return this.time;
    }
}
