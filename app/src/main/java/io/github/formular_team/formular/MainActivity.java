package io.github.formular_team.formular;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.formular_team.formular.server.Game;
import io.github.formular_team.formular.server.ServerController;
import io.github.formular_team.formular.server.SimpleServer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ServerController controller;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        /*final ArFragment fragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        fragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
            @Override
            public void onUpdate(final FrameTime frameTime) {}
        });
        ModelRenderable.builder()
            .setSource(this, Uri.parse("teapot.sfb"))
            .build()
            .thenAccept(renderable -> {
                Node node = new Node();
                node.setParent(fragment.getArSceneView().getScene());
                node.setRenderable(renderable);
            })
            .exceptionally(throwable -> {
                Log.e("formular", "Unable to load Renderable.", throwable);
                return null;
            });*/

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Game game = new Game();
        this.controller = ServerController.create(SimpleServer.create(game, 20));
        this.controller.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.controller.stop();
    }
}
