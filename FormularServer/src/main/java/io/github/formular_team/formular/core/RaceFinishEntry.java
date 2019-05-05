package io.github.formular_team.formular.core;

public final class RaceFinishEntry {
    private final User user;

    private final int position;

    private final long time;

    public RaceFinishEntry(final User user, final int position, final long time) {
        this.user = user;
        this.position = position;
        this.time = time;
    }

    public User getUser() {
        return this.user;
    }

    public int getPosition() {
        return this.position;
    }

    public long getTime() {
        return this.time;
    }
}
