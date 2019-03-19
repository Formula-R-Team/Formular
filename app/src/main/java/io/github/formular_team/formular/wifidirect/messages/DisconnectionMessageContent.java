package io.github.formular_team.formular.wifidirect.messages;

import io.github.formular_team.formular.wifidirect.WroupDevice;

public class DisconnectionMessageContent {

    private WroupDevice wroupDevice;

    public void setWroupDevice(WroupDevice wroupDevice) {
        this.wroupDevice = wroupDevice;
    }

    public WroupDevice getWroupDevice() {
        return wroupDevice;
    }
}
