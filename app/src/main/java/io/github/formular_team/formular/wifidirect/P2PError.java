package io.github.formular_team.formular.wifidirect;

public enum P2PError {

    ERROR(0), P2P_NOT_SUPPORTED(1), BUSSY(2);

    private int reason;

    P2PError(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    public static P2PError fromReason(int reason) {
        for (P2PError wiFiP2PError : P2PError.values()) {
            if (reason == wiFiP2PError.reason) {
                return wiFiP2PError;
            }
        }

        return null;
    }

}
