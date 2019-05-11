package io.github.formular_team.formular.core.game;

import java.util.Optional;

import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.math.Vector2;

public interface GameView extends Game {
    Kart createKart(final int uniqueId, final Color color, final Vector2 position, final float rotation);

    void addCourse(final Course course);

    void setCount(int count);

    void setLap(int lap);

    void setPosition(int position);

    void onFinish();

    Optional<Kart> removeKart(final int uniqueId);

    Optional<Kart> getKart(final int uniqueId);

    Kart.ControlState getControlState();
}
