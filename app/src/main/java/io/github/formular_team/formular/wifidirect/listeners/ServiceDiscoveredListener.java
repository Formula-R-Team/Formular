package io.github.formular_team.formular.wifidirect.listeners;

import java.util.List;

import io.github.formular_team.formular.wifidirect.P2PError;
import io.github.formular_team.formular.wifidirect.WroupServiceDevice;

public interface ServiceDiscoveredListener {
    void onNewServiceDeviceDiscovered(WroupServiceDevice serviceDevice);

    void onFinishServiceDeviceDiscovered(List<WroupServiceDevice> serviceDevices);

    void onError(P2PError wiFiP2PError);
}
