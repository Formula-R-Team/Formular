package io.github.formular_team.formular;

import android.support.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.BaseArFragment;

import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.kart.Kart;

public interface ArActivity {
    User getUser();

    Rectifier createRectifier();

    Scene getScene();

    void setOnTapArPlaneListener(final @Nullable BaseArFragment.OnTapArPlaneListener listener);

    void createRace(final Anchor anchor, final Course course);

    void startRace();

    void joinRace(final Anchor anchor);

    void addRaceListener(final RaceView view);

    void onSteer(final Kart.ControlState state);
}
