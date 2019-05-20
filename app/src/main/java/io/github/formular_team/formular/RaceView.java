package io.github.formular_team.formular;

import android.support.annotation.StringRes;

public interface RaceView {
    void setCount(@StringRes int resID);

    void setPosition(@StringRes int resID, final int position);

    void setLap(@StringRes int resID, final int lap, final int lapCount);

    void setFinish();
}
