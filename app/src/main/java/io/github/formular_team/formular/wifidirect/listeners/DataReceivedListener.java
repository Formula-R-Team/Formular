package io.github.formular_team.formular.wifidirect.listeners;

import io.github.formular_team.formular.wifidirect.messages.MessageWrapper;

public interface DataReceivedListener {
    void onDataReceived(MessageWrapper messageWrapper);
}
