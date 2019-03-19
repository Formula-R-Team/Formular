package io.github.formular_team.formular.wifidirect;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiDirectUtils {
    private static final String TAG = WifiDirectUtils.class.getSimpleName();

    public static void clearServiceRequest(P2PInstance wiFiP2PInstance) {
        wiFiP2PInstance.getWifiP2pManager().clearServiceRequests(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Success clearing service request");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error clearing service request: " + reason);
            }

        });
    }

    public static void clearLocalServices(P2PInstance wiFiP2PInstance) {
        wiFiP2PInstance.getWifiP2pManager().clearLocalServices(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Local services cleared");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error clearing local services: " + P2PError.fromReason(reason));
            }

        });
    }

    public static void cancelConnect(P2PInstance wiFiP2PInstance) {
        wiFiP2PInstance.getWifiP2pManager().cancelConnect(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Connect canceled successfully");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error canceling connect: " + P2PError.fromReason(reason));
            }

        });
    }

    public static void removeGroup(final P2PInstance wiFiP2PInstance) {
        wiFiP2PInstance.getWifiP2pManager().requestGroupInfo(wiFiP2PInstance.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup group) {
                wiFiP2PInstance.getWifiP2pManager().removeGroup(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Group removed: " + group.getNetworkName());
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Fail disconnecting from group. Reason: " + P2PError.fromReason(reason));
                    }
                });
            }
        });
    }

    public static void stopPeerDiscovering(P2PInstance wiFiP2PInstance) {
        wiFiP2PInstance.getWifiP2pManager().stopPeerDiscovery(wiFiP2PInstance.getChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Peer discovering stopped");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error stopping peer discovering: " + P2PError.fromReason(reason));
            }

        });
    }

}
