package io.github.formular_team.formular.wifidirect.messages;

import com.skozubenko.wifip2p_3.WifiDirect.WroupDevice;

public class DisconnectionMessageContent {

    private WroupDevice wroupDevice;

    public void setWroupDevice(WroupDevice wroupDevice) {
        this.wroupDevice = wroupDevice;
    }

    public WroupDevice getWroupDevice() {
        return wroupDevice;
    }
}
