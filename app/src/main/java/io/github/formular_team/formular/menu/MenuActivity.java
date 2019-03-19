package io.github.formular_team.formular.menu;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.*;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import io.github.formular_team.formular.MainActivity;
import io.github.formular_team.formular.R;
import io.github.formular_team.formular.lobby.LobbyActivity;
import io.github.formular_team.formular.wifidirect.*;
import io.github.formular_team.formular.wifidirect.listeners.*;
import io.github.formular_team.formular.wifidirect.BroadcastReceiver;

public class MenuActivity extends AppCompatActivity implements GroupCreationDialog.GroupCreationAcceptButtonListener {
    private static final String TAG = MenuActivity.class.getSimpleName();

    Button btnStartGame, btnJoinGame, btnStartHosting;

    WifiManager wifiManager;
    WroupClient client;
    WroupService service;
    BroadcastReceiver broadcastReceiver;
    GroupCreationDialog groupCreationDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);       //this.requestWindowFeature(Window.FEATURE_NO_TITLE);   for normal Activity
        this.setContentView(R.layout.activity_menu);

        this.setupButtons();

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.makeSurePermissionsAreEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        this.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {                       //threw this try in here to avoid exception on screen rotation. java.lang.IllegalArgumentException: Receiver not registered: null
            this.unregisterReceiver(broadcastReceiver);
        }
        catch(Exception e) {
            Log.i(TAG, "Exception: " + e.getMessage());
        }

    }

    private void setupButtons() {

        this.btnStartGame = findViewById(R.id.btnStartGame);
        this.btnJoinGame = findViewById(R.id.btnJoinGame);
        this.btnStartHosting = findViewById(R.id.btnStartHosting);

        this.btnStartHosting.setOnClickListener(this::onBtnHostGame);

        this.btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onBtnHostGame(final View view) {
        if(!this.wifiManager.isWifiEnabled())
            this.wifiManager.setWifiEnabled(true);

        this.groupCreationDialog = new GroupCreationDialog().setActivity(this);
        this.groupCreationDialog.addGroupCreationAcceptListener(MenuActivity.this);
        this.groupCreationDialog.show(getSupportFragmentManager(), GroupCreationDialog.class.getSimpleName());
    }


    @Override
    public void onAcceptButtonListener(final String groupName) {
        if (!groupName.isEmpty()) {
            service = WroupService.getInstance(getApplicationContext());
            service.registerService(groupName, new ServiceRegisteredListener() {

                @Override
                public void onSuccessServiceRegistered() {

                    Log.i(TAG, "Group created.");
                    moveUserIntoLobby(groupName, true);

//                    startGroupChatActivity(groupName, true);
                    groupCreationDialog.dismiss();
                }

                @Override
                public void onErrorServiceRegistered(P2PError wiFiP2PError) {
                    Toast.makeText(getApplicationContext(), "Error creating group", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(getApplicationContext(), "Please, insert a group name", Toast.LENGTH_SHORT).show();
        }

        service.setClientConnectedListener(new ClientConnectedListener() {
            @Override
            public void onClientConnected(WroupDevice wroupDevice) {
                Toast.makeText(getApplicationContext(), "New device connected to Race Game Service. Name: " + wroupDevice.getDeviceName() + "; Mac: " + wroupDevice.getDeviceMac(), Toast.LENGTH_SHORT).show();
            }
        });

        service.setClientDisconnectedListener(new ClientDisconnectedListener() {
            @Override
            public void onClientDisconnected(WroupDevice wroupDevice) {
                Toast.makeText(getApplicationContext(), "Device disconnected from Race Game Service. Name: " + wroupDevice.getDeviceName() + "; Mac: " + wroupDevice.getDeviceMac(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveUserIntoLobby(String groupName, boolean isGroupOwner) {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra(LobbyActivity.EXTRA_GROUP_NAME, groupName);
        intent.putExtra(LobbyActivity.EXTRA_IS_GROUP_OWNER, isGroupOwner);
        startActivity(intent);
    }


    public void makeSurePermissionsAreEnabled() {
        if (ActivityCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
}
