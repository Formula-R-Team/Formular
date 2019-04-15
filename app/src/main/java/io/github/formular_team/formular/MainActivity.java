package io.github.formular_team.formular;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.KartModel;
import io.github.formular_team.formular.core.SimpleGameModel;

public class MainActivity extends FormularActivity {
    private Button btnRace;

    private Button btnCustomize;

    private SceneView sceneView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupButtons();
        this.sceneView = this.findViewById(R.id.ar_scene);
        this.sceneView.setBackgroundColor(0);
        final WeakOptional<MainActivity> act =  WeakOptional.of(this);
        ModelRenderable.builder()
            .setSource(this, R.raw.kart)
            .build()
            .thenCombine(
                ModelRenderable.builder()
                    .setSource(this, R.raw.wheel)
                    .build(),
                (body, wheel) -> act.map(activity -> {
                    body.getMaterial(0).setFloat4("baseColor", new Color(0xFF42A2DD));
                    final KartNode kart = KartNode.create(new KartModel(new SimpleGameModel(), 0, RaceActivity.createKartDefinition()), body, wheel);
                    final Node transform = new Node();
                    transform.setLocalPosition(new Vector3(0.0F, -0.5F, -1.0F));
                    transform.setLocalScale(Vector3.one().scaled(0.5F));
//                    final Renderable r = ShapeFactory.makeCube(new Vector3(1.0F, 1.0F, 1.0F), new Vector3(0.0F, 0.0F, 0.0F), body.getMaterial(0));
//                    transform.setRenderable(r);
                    transform.addChild(kart);
                    activity.sceneView.getScene().addChild(transform);
                    return null;
                }))
        .exceptionally(t -> act.map(activity -> {
            new AlertDialog.Builder(activity)
                .setMessage(t.getMessage())
                .setTitle("Error")
                .create()
                .show();
            return null;
        }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sceneView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            this.sceneView.resume();
        } catch (final CameraNotAvailableException e) {
            throw new AssertionError(e);
        }
    }

    private void setupButtons() {
        this.btnRace = this.findViewById(R.id.btnRace);
        this.btnCustomize = this.findViewById(R.id.btnCustomize);
        this.btnCustomize.setOnClickListener(this.activityChange(CustomizeActivity.class));
        this.btnRace.setOnClickListener(this.activityChange(NewRaceActivity.class));
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}
