package io.github.formular_team.formular.wifidirect.messages;

import java.util.List;

import io.github.formular_team.formular.wifidirect.WroupDevice;

public class RegisteredDevicesMessageContent {

    private List<WroupDevice> devicesRegistered;

    public List<WroupDevice> getDevicesRegistered() {
        return devicesRegistered;
    }

    public void setDevicesRegistered(List<WroupDevice> devicesRegistered) {
        this.devicesRegistered = devicesRegistered;
    }
}
