package io.github.formular_team.formular.wifidirect.messages;

import com.skozubenko.wifip2p_3.WifiDirect.WroupDevice;

public class RegistrationMessageContent {

    private WroupDevice wroupDevice;

    public WroupDevice getWroupDevice() {
        return wroupDevice;
    }

    public void setWroupDevice(WroupDevice wroupDevice) {
        this.wroupDevice = wroupDevice;
    }

}
