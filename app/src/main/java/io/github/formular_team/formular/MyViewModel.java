package io.github.formular_team.formular;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.github.formular_team.formular.core.race.RaceConfiguration;

public class MyViewModel extends android.arch.lifecycle.ViewModel {
    private MutableLiveData<RaceConfiguration> raceConfiguration = new MutableLiveData<RaceConfiguration>();

    public LiveData<RaceConfiguration> getRaceConfiguration() {
        return raceConfiguration;
    }

    public void setRaceConfiguration(RaceConfiguration rc) {
        raceConfiguration.setValue(rc);
    }
}
