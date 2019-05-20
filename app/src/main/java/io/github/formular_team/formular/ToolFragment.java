package io.github.formular_team.formular;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.core.course.Capture;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.course.CourseMetadata;
import io.github.formular_team.formular.core.course.CourseReader;
import io.github.formular_team.formular.core.race.RaceConfiguration;
import io.github.formular_team.formular.core.server.Client;
import io.github.formular_team.formular.core.server.Endpoint;
import io.github.formular_team.formular.core.server.EndpointController;
import io.github.formular_team.formular.core.server.Server;
import io.github.formular_team.formular.core.server.SimpleClient;

public class ToolFragment extends Fragment {
    private static final String TAG = "ToolFragment";

    private View view;

    private EndpointController<Client> clientController;

    private EndpointController<Server> serverController;

    private ArActivity activity;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_tool, container, false);
        final BottomNavigationView view = this.view.findViewById(R.id.toolbar);
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                switch (item.getItemId()) {
                case R.id.tool_shop:
                    break;
                case R.id.tool_create:
                    ToolFragment.this.activity.setOnTapArPlaneListener(new CreateListener());
                    break;
                case R.id.tool_join:
                    break;
                }
                return true;
            }
        });
        return this.view;
    }

    private class CreateListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(final HitResult result, final Plane plane, final MotionEvent event) {
            final Capture capture;
            try (final Capturer capturer = new Capturer(ToolFragment.this.activity.createRectifier())) {
                capture = capturer.capture(result.getHitPose(), 0.25F, 200);
            }
            final CourseReader reader = new CourseReader();
            reader.read(capture,
                CourseMetadata.create(ToolFragment.this.activity.getUser(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "My Circuit"),
                new CourseReader.ResultConsumer() {
                    @Override
                    public void onSuccess(final Course course) {
                        final AnchorNode node = new AnchorNode(result.createAnchor());
                        ToolFragment.this.activity.getScene().addChild(node);
                    }

                    @Override
                    public void onFail() {}
                });
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

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof ArActivity) {
            this.activity = (ArActivity) context;
        } else {
            throw new RuntimeException(context + " must implement ArActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    private void startRace(final Node surface, final RaceConfiguration config, final Course course) {
        this.startServer();
        this.startClient(surface, new InetSocketAddress(InetAddress.getLoopbackAddress(), Endpoint.DEFAULT_PORT));
        this.clientController.submitJob(Endpoint.Job.of(c -> c.createRace(config, course)));
        /*final Button start = this.findViewById(R.id.start);
        start.setVisibility(View.VISIBLE);
        start.setOnClickListener(v -> {
            this.clientController.submitJob(Endpoint.Job.of(Client::startRace));
            v.setVisibility(View.INVISIBLE);
            this.view.<TextView>findViewById(R.id.ip).setVisibility(View.INVISIBLE);
            this.enterInGame();
        });*/
    }

    /*private void promptConnect(final Node node) {
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
                    this.enterInGame();
                }
            }
        });
        dialog.show();
        final EditText addressText = dialog.findViewById(R.id.host_address);
        if (addressText != null) {
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            addressText.setText(settings.getString("lastHostAddress", ""));
        }
    }*/

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

    /*private Node createAnchor(final HitResult result) {
        final AnchorNode anchor = new AnchorNode(result.createAnchor());
        this.arFragment.getArSceneView().getScene().addChild(anchor);
        return anchor;
    }*/

    /*@Override
    public void onSteer(final KartModel.ControlState state) {
        if (this.clientController != null) {
            final Kart.ControlState copy = new SimpleControlState().copy(state);
            this.clientController.submitJob(Endpoint.Job.of(c -> {
                // TODO: better client state management
                c.getGame().getControlState().copy(copy);
            }));
        }
    }*/

    private void startClient(final Node surface, final InetSocketAddress address) {
        try {
            this.clientController = EndpointController.create(SimpleClient.open(address, this.user, ArGameView.create(this, this.interfaceFragment, this.arFragment.getArSceneView().getScene(), surface, this.factory), 30));
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
}
