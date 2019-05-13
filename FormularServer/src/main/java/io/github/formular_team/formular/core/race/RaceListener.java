package io.github.formular_team.formular.core.race;

import java.util.List;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.RaceFinishEntry;

public interface RaceListener {
    default void onBegin() {}

    default void onFinish(final Driver driver) {}

    default void onEnd(final List<RaceFinishEntry> entries) {}

    default void onCountDown(final int count) {}

    default void onProgressChange(final Driver driver, final float progress) {}

    default void onPositionChange(final Driver driver, final int position) {}

    default void onLapChange(final Driver driver, final int lap) {}

    default void onMoveForward(final Driver driver) {}

    default void onMoveBackward(final Driver driver) {}
}
