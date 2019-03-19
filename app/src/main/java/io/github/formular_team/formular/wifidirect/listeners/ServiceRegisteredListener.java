package io.github.formular_team.formular.wifidirect.listeners;

import io.github.formular_team.formular.wifidirect.P2PError;

public interface ServiceRegisteredListener {
    void onSuccessServiceRegistered();

    void onErrorServiceRegistered(P2PError wiFiP2PError);
}
