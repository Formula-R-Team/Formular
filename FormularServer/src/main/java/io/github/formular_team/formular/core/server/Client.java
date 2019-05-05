package io.github.formular_team.formular.core.server;

import io.github.formular_team.formular.core.Course;
import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.race.RaceConfiguration;

public interface Client extends Endpoint<Client> {
    GameView getGame();

    void createRace(RaceConfiguration config, Course course);

    void joinRace();

    void startRace();
}
