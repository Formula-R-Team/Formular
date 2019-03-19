package io.github.formular_team.formular.wifidirect.listeners;

import com.skozubenko.wifip2p_3.WifiDirect.WroupDevice;

public interface ClientDisconnectedListener {
    void onClientDisconnected(WroupDevice wroupDevice);
}
