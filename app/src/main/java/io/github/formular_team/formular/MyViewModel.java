package io.github.formular_team.formular;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class MyViewModel extends android.arch.lifecycle.ViewModel {
    private boolean spectators;

    public boolean isSpectators() {
        return spectators;
    }

    public void setSpectators(boolean spectators) {
        this.spectators = spectators;
    }
}
