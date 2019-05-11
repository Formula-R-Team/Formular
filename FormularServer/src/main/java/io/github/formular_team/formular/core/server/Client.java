package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.game.GameView;
import io.github.formular_team.formular.core.race.RaceConfiguration;

public interface Client extends Endpoint<Client> {
    GameView getGame();

    void createRace(RaceConfiguration config, Course course);

    void joinRace();

    void startRace();
}
