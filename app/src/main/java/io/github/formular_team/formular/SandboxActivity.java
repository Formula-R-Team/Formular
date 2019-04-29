package io.github.formular_team.formular;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import io.github.formular_team.formular.ar.ArGameView;
import io.github.formular_team.formular.core.SimpleGameModel;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.server.Endpoint;
import io.github.formular_team.formular.core.server.EndpointController;
import io.github.formular_team.formular.core.server.SimpleClient;
import io.github.formular_team.formular.core.server.SimpleServer;

public class SandboxActivity extends FormularActivity {
    private static final String TAG = "SandboxActivity";

    public static final String EXTRA_HOST = "host";

    private User user;

    private boolean host;

    private View pad, wheel;

    private ArFragment arFragment;

    private KartNodeFactory factory;

    private EndpointController controller;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sandbox);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String namePref = prefs.getString("prefName", "Player 1");
        final int colorPref = prefs.getInt("primaryColor", 0xFFFF007F);
        this.user = User.create(namePref, colorPref);
        final Intent intent = this.getIntent();
        if (intent != null) {
            this.host = intent.getBooleanExtra(EXTRA_HOST, true);
        }
        this.pad = this.findViewById(R.id.pad);
        this.wheel = this.findViewById(R.id.wheel);
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        if (this.arFragment == null) {
            throw new AssertionError("Missing ar fragment");
        }
        this.arFragment.setOnTapArPlaneListener((result, plane, event) -> {
            final AnchorNode anchor = new AnchorNode(result.createAnchor());
            anchor.setLocalScale(Vector3.one().scaled(0.075F));
            this.arFragment.getArSceneView().getScene().addChild(anchor);
            if (!this.host) {
                this.promptConnect(anchor);
            }
        });
//        this.pad.setOnTouchListener(new KartController(this.kart, this.pad, this.wheel));
        final WeakOptional<SandboxActivity> act = WeakOptional.of(this);
        SimpleKartNodeFactory.create(this, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear)
            .thenAccept(factory -> act.ifPresent(activity -> activity.factory = factory));
    }

    private void promptConnect(final Node node) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Connect to server")
            .setView(R.layout.host_address_prompt)
            .create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (d, which) -> {
            final EditText addressText = dialog.findViewById(R.id.host_address);
            if (addressText != null) {
                final InetSocketAddress address = this.parseAddress(addressText.getText().toString());
                if (address != null) {
                    this.startClient(node, address);
                }
            }
        });
        dialog.show();
    }

    private InetSocketAddress parseAddress(final String address) {
        final URI uri;
        try {
            uri = new URI("A", address, null, null, null);
        } catch (final URISyntaxException e) {
            return null;
        }
        final String host = uri.getHost();
        if (host == null) {
            return null;
        }
        final int port;
        if (uri.getPort() == -1) {
            port = Endpoint.DEFAULT_PORT;
        } else {
            port = uri.getPort();
        }
        return new InetSocketAddress(host, port);
    }

    private void startClient(final Node surface, final InetSocketAddress address) {
        try {
            this.controller = EndpointController.create(SimpleClient.open(address, this.user, ArGameView.create(this, this.arFragment.getArSceneView().getScene(), surface, this.factory), 20));
            this.controller.start();
        } catch (final IOException e) {
            Log.e(TAG, "Error creating client", e);
        }
    }

    private void startServer() {
        try {
            this.controller = EndpointController.create(SimpleServer.open(new InetSocketAddress(Endpoint.DEFAULT_PORT), new SimpleGameModel(), 20));
            this.controller.start();
        } catch (final IOException e) {
            Log.e(TAG, "Error creating server", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.controller != null) {
            this.controller.stop();
        }
    }
}
