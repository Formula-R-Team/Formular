package io.github.formular_team.formular;

import android.support.annotation.Nullable;

import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.BaseArFragment;

import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.User;

public interface ArActivity {
    User getUser();

    Rectifier createRectifier();

    Scene getScene();

    void setOnTapArPlaneListener(@Nullable BaseArFragment.OnTapArPlaneListener listener);
}
