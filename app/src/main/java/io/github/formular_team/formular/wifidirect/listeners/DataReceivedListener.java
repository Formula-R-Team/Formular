package io.github.formular_team.formular.wifidirect.listeners;

import android.net.wifi.p2p.WifiP2pInfo;

import com.skozubenko.wifip2p_3.WifiDirect.messages.MessageWrapper;

public interface DataReceivedListener {
    void onDataReceived(MessageWrapper messageWrapper);
}
