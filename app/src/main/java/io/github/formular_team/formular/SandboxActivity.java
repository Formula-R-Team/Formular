package io.github.formular_team.formular;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import io.github.formular_team.formular.ar.ArGameView;
import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.Capture;
import io.github.formular_team.formular.core.Course;
import io.github.formular_team.formular.core.CourseReader;
import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.SimpleGameModel;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.race.RaceConfiguration;
import io.github.formular_team.formular.core.server.Client;
import io.github.formular_team.formular.core.server.Endpoint;
import io.github.formular_team.formular.core.server.EndpointController;
import io.github.formular_team.formular.core.server.Server;
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

    private EndpointController<Client> clientController;

    private EndpointController<Server> serverController;

    private Node createAnchor(final HitResult result) {
        final AnchorNode anchor = new AnchorNode(result.createAnchor());
        this.arFragment.getArSceneView().getScene().addChild(anchor);
        return anchor;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sandbox);
        this.user = AppPreferences.getUser(PreferenceManager.getDefaultSharedPreferences(this));
        final Intent intent = this.getIntent();
        if (intent != null) {
            this.host = intent.getBooleanExtra(EXTRA_HOST, true);
        }
        this.pad = this.findViewById(R.id.pad);
        this.wheel = this.findViewById(R.id.wheel);
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        if (this.arFragment == null) {
            throw new RuntimeException("Missing ar fragment");
        }
        this.arFragment.setOnTapArPlaneListener((result, plane, event) -> {
            final Frame frame = this.arFragment.getArSceneView().getArFrame();
            if (this.factory != null && this.clientController == null && frame != null) {
                if (this.host) {
                    final Capture capture;
                    try (final Capturer capturer = new Capturer(new Rectifier(frame))) {
                        capture = capturer.capture(result.getHitPose(), 0.25F, 200);
                    } catch (final NotYetAvailableException e) {
                        throw new RuntimeException(e);
                    }
                    final CourseReader reader = new CourseReader(this.user);
                    reader.read(capture, new CourseReader.Callback() {
                        @Override
                        public void success(final Course course) {
                            SandboxActivity.this.startRace(SandboxActivity.this.createAnchor(result), RaceConfiguration.builder().build(), course);
                        }

                        @Override
                        public void fail() {}
                    });
                } else {
                    this.promptConnect(this.createAnchor(result));
                }
            }
        });
        this.pad.setOnTouchListener(new KartController(new SimpleControlState(), state -> {
            if (this.clientController != null) {
                final Kart.ControlState copy = new SimpleControlState().copy(state);
                this.clientController.submitJob(Endpoint.Job.of(c -> {
                    // TODO: better client state management
                    c.getGame().getControlState().copy(copy);
                }));
            }
        }, this.wheel));
        final WeakOptional<SandboxActivity> act = WeakOptional.of(this);
        SimpleKartNodeFactory.create(this, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear)
            .thenAccept(factory -> act.ifPresent(activity -> activity.factory = factory));
    }

    private void startRace(final Node surface, final RaceConfiguration config, final Course course) {
        this.startServer();
        this.startClient(surface, new InetSocketAddress(InetAddress.getLoopbackAddress(), Endpoint.DEFAULT_PORT));
        this.clientController.submitJob(Endpoint.Job.of(c -> c.createRace(config, course)));
        final Button start = this.findViewById(R.id.start);
        start.setVisibility(View.VISIBLE);
        start.setOnClickListener(v -> {
            this.clientController.submitJob(Endpoint.Job.of(Client::startRace));
            v.setVisibility(View.INVISIBLE);
            this.<TextView>findViewById(R.id.ip).setVisibility(View.INVISIBLE);
            this.pad.setVisibility(View.VISIBLE);
        });
    }

    private void promptConnect(final Node node) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Connect to server")
            .setView(R.layout.host_address_prompt)
            .create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (d, which) -> {
            final EditText addressText = dialog.findViewById(R.id.host_address);
            if (addressText != null) {
                final String text = addressText.getText().toString();
                final InetSocketAddress address = this.parseAddress(text);
                if (address != null) {
                    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    settings.edit().putString("lastHostAddress", text).apply();
                    this.startClient(node, address);
                    this.clientController.submitJob(Endpoint.Job.of(Client::joinRace));
                }
            }
        });
        dialog.show();
        final EditText addressText = dialog.findViewById(R.id.host_address);
        if (addressText != null) {
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            addressText.setText(settings.getString("lastHostAddress", ""));
        }
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
            this.clientController = EndpointController.create(SimpleClient.open(address, this.user, ArGameView.create(this, this.findViewById(R.id.count), this.findViewById(R.id.position), this.findViewById(R.id.lap) , this.arFragment.getArSceneView().getScene(), surface, this.factory), 30));
            this.clientController.start();
        } catch (final IOException e) {
            Log.e(TAG, "Error creating client", e);
        }
    }

    private void startServer() {
        try {
            this.serverController = EndpointController.create(SimpleServer.open(new InetSocketAddress(Endpoint.DEFAULT_PORT), new SimpleGameModel(), 30));
            this.serverController.start();
        } catch (final IOException e) {
            Log.e(TAG, "Error creating server", e);
        }
        final WifiManager wifi = this.getSystemService(WifiManager.class);
        final String ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        final TextView ipText = this.findViewById(R.id.ip);
        ipText.setVisibility(View.VISIBLE);
        ipText.setText(ip);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.clientController != null) {
            this.clientController.stop();
        }
        if (this.serverController != null) {
            this.serverController.stop();
        }
    }
}
