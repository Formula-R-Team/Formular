package io.github.formular_team.formular.wifidirect.messages;

import com.skozubenko.wifip2p_3.WifiDirect.WroupDevice;

import java.util.List;

public class RegisteredDevicesMessageContent {

    private List<WroupDevice> devicesRegistered;

    public List<WroupDevice> getDevicesRegistered() {
        return devicesRegistered;
    }

    public void setDevicesRegistered(List<WroupDevice> devicesRegistered) {
        this.devicesRegistered = devicesRegistered;
    }
}
