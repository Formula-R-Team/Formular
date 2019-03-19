package io.github.formular_team.formular.wifidirect.listeners;

import android.net.wifi.p2p.WifiP2pInfo;

public interface PeerConnectedListener {
    void onPeerConnected(WifiP2pInfo wifiP2pInfo);
}
