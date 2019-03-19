package io.github.formular_team.formular.wifidirect;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.skozubenko.wifip2p_3.WifiDirect.listeners.PeerConnectedListener;
import com.skozubenko.wifip2p_3.WifiDirect.listeners.ServiceDisconnectedListener;

public class P2PInstance implements WifiP2pManager.ConnectionInfoListener {
    private static final String TAG = P2PInstance.class.getSimpleName();

    private static P2PInstance instance;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver broadcastReceiver;

    private WroupDevice thisDevice;

    private PeerConnectedListener peerConnectedListener;
    private ServiceDisconnectedListener serviceDisconnectedListener;

    private P2PInstance() {
    }

    private P2PInstance(Context context) {
        this();

        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
        broadcastReceiver = new BroadcastReceiver(this);
    }

    public static P2PInstance getInstance(Context context) {
        if (instance == null) {
            instance = new P2PInstance(context);
        }

        return instance;
    }


    public void startPeerDiscovering() {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Peers discovering initialized");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Error initiating peer disconvering. Reason: " + reason);
            }
        });
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (peerConnectedListener != null) {
            peerConnectedListener.onPeerConnected(info);
        }
    }

    public void onServerDeviceDisconnected() {
        if (serviceDisconnectedListener != null) {
            serviceDisconnectedListener.onServerDisconnectedListener();
        }
    }


    public void setPeerConnectedListener(PeerConnectedListener peerConnectedListener) {
        this.peerConnectedListener = peerConnectedListener;
    }

    public void setServerDisconnectedListener(ServiceDisconnectedListener serviceDisconnectedListener) {
        this.serviceDisconnectedListener = serviceDisconnectedListener;
    }

    public void setThisDevice(WroupDevice thisDevice) {
        this.thisDevice = thisDevice;
    }

    public WroupDevice getThisDevice() {
        return thisDevice;
    }

    public WifiP2pManager getWifiP2pManager() {
        return wifiP2pManager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }
}
