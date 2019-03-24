package io.github.formular_team.formular.server;

public interface RaceListener {
    default void onBegin() {}

    default void onEnd() {}

    default void onCount(final int count) {}

    default void onProgress(final Driver driver, final float progress) {}

    default void onPosition(final Driver driver, final int position) {}

    default void onLap(final Driver driver, final int lap) {}

    default void onForward(final Driver driver) {}

    default void onReverse(final Driver driver) {}
}
