package io.github.formular_team.formular;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.EditText;

import com.google.ar.core.Frame;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import io.github.formular_team.formular.ar.ArGameView;
import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.SimpleGameModel;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.race.RaceConfiguration;
import io.github.formular_team.formular.core.server.Client;
import io.github.formular_team.formular.core.server.Endpoint;
import io.github.formular_team.formular.core.server.EndpointController;
import io.github.formular_team.formular.core.server.Server;
import io.github.formular_team.formular.core.server.SimpleClient;
import io.github.formular_team.formular.core.server.SimpleServer;

public class SandboxActivity extends FormularActivity implements ArActivity {
    private static final String TAG = "SandboxActivity";

    private User user;

    private ArFragment arFragment;

    private NavController controller;

    private KartNodeFactory factory;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sandbox);
        this.user = AppPreferences.getUser(PreferenceManager.getDefaultSharedPreferences(this));
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        if (this.arFragment == null) {
            throw new IllegalStateException("ar fragment is null");
        }
        this.controller = Navigation.findNavController(this, R.id.ar_interface);
        final CompletableFuture<Texture> texture = Texture.builder()
            .setSampler(Texture.Sampler.builder()
                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                .build())
            .setSource(this, R.drawable.blueprint).build();
        this.arFragment.getArSceneView().getPlaneRenderer().getMaterial().thenAcceptBoth(texture, (mat, tex) -> {
            mat.setTexture(PlaneRenderer.MATERIAL_TEXTURE, tex);
            mat.setFloat2(PlaneRenderer.MATERIAL_UV_SCALE, 4.0F, 4.0F);
        });
        final WeakOptional<SandboxActivity> act = WeakOptional.of(this);
        SimpleKartNodeFactory.create(this, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear)
            .thenAccept(factory -> act.ifPresent(activity -> activity.factory = factory));
        final OrientationEventListener listener = new OrientationEventListener(this) {
            private final int[] orientations = {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            };

            private int orientation = -1;

            @Override
            public void onOrientationChanged(final int orientation) {
                if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    final int o = this.orientations[(orientation + 360 - 45) / 90 % 4];
                    if (this.orientation != o) {
                        SandboxActivity.this.setRequestedOrientation(o);
                        this.orientation = o;
                    }
                }
            }
        };
        if (listener.canDetectOrientation()) {
            listener.enable();
        }
        this.listeners = new ArrayList<>();
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public @Nullable Rectifier createRectifier() {
        final Frame frame = this.arFragment.getArSceneView().getArFrame();
        if (frame == null) {
            throw new IllegalStateException("frame is null");
        }
        try {
            return new Rectifier(frame);
        } catch (final NotYetAvailableException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Scene getScene() {
        return this.arFragment.getArSceneView().getScene();
    }

    @Override
    public void setOnTapArPlaneListener(final @Nullable BaseArFragment.OnTapArPlaneListener listener) {
        this.arFragment.setOnTapArPlaneListener(listener);
    }

    private List<RaceView> listeners;

    @Override
    public void addRaceListener(final RaceView view) {
        this.listeners.add(view);
    }

    @Override
    public void onSteer(final Kart.ControlState state) {
        if (this.clientController != null) {
            final Kart.ControlState copy = new SimpleControlState().copy(state);
            this.clientController.submitJob(Endpoint.Job.of(c -> c.getGame().getControlState().copy(copy)));
        }
    }

    @Override
    public void createRace(final Node anchor, final Course course) {
        this.createGame(anchor);
        this.startServer();
        this.startClient(new InetSocketAddress(InetAddress.getLoopbackAddress(), Endpoint.DEFAULT_PORT));
        this.clientController.submitJob(Endpoint.Job.of(c -> c.createRace(RaceConfiguration.builder().build(), course)));
        this.controller.navigate(R.id.race_fragment);
    }

    private Node tmp;

    @Override
    public Node getAnchor() {
        return this.tmp;
    }

    @Override
    public void customize(final Node anchorNode) {
        this.tmp = anchorNode;
        this.controller.navigate(R.id.customize_fragment);
    }

    @Override
    public boolean isHost() {
        return this.serverController != null;
    }

    @Override
    public void removeAnchor() {
        if (this.tmp != null) {
            this.getScene().removeChild(this.tmp);
            this.tmp = null;
        }
    }

    @Override
    public void startRace() {
        if (this.clientController != null) {
            this.clientController.submitJob(Endpoint.Job.of(Client::startRace));
        }
    }

    private EndpointController<Client> clientController;

    private EndpointController<Server> serverController;

    private ArGameView game;

    @Override
    public void joinRace(final Node anchor) {
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
                    this.createGame(anchor);
                    this.startClient(address);
                    this.clientController.submitJob(Endpoint.Job.of(Client::joinRace));
                    this.controller.navigate(R.id.race_fragment);
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

    private void createGame(final Node node) {
        this.game = ArGameView.create(this, new RaceView() {
            private Iterable<RaceView> listeners() {
                return SandboxActivity.this.listeners;
            }

            @Override
            public void setCount(final int resID) {
                for (final RaceView view : this.listeners()) {
                    view.setCount(resID);
                }
            }

            @Override
            public void setPosition(final int resID, final int position) {
                for (final RaceView view : this.listeners()) {
                    view.setPosition(resID, position);
                }
            }

            @Override
            public void setLap(final int resID, final int lap, final int lapCount) {
                for (final RaceView view : this.listeners()) {
                    view.setLap(resID, lap, lapCount);
                }
            }

            @Override
            public void setFinish() {
                for (final RaceView view : this.listeners()) {
                    view.setFinish();
                }
            }
        }, this.getScene(), node, this.factory);
        this.arFragment.getArSceneView().getPlaneRenderer().setVisible(false);
    }

    private void startClient(final InetSocketAddress address) {
        try {
            this.clientController = EndpointController.create(SimpleClient.open(address, this.user, this.game, 30));
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.clientController != null) {
            this.clientController.stop();
        }
        if (this.serverController != null) {
            this.serverController.stop();
        }
    }
}
