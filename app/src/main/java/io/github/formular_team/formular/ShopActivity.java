package io.github.formular_team.formular;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.SeekBar;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;

import org.hsluv.HUSLColorConverter;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.KartDefinition;
import io.github.formular_team.formular.core.KartModel;
import io.github.formular_team.formular.core.SimpleGameModel;

public final class ShopActivity extends FormularActivity {
    private SceneView sceneView;

    private HueSlider slider;

    private KartNode kart;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_shop);
        this.sceneView = this.findViewById(R.id.ar_scene);
        this.slider = this.findViewById(R.id.hue);
        this.slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (ShopActivity.this.kart != null) {
                    final double[] rgb = HUSLColorConverter.hsluvToRgb(new double[] { progress / 100.0F * 360.0F, 85.0F, 30.0F });
                    ShopActivity.this.kart.setColor(new Color((float) rgb[0], (float) rgb[1], (float) rgb[2]));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });
        final WeakOptional<ShopActivity> act = WeakOptional.of(this);
        ModelRenderable.builder()
            .setSource(this, R.raw.shop_room)
            .build()
            .thenApply(r -> {
                final Node node = new Node();
//                node.setRenderable(r);
                return node;
            })
            .thenCombine(
                ModelRenderable.builder()
                    .setSource(this, R.raw.shop_platter)
                    .build()
                    .thenApply(r -> {
                        final Node node = new Node() {
                            float theta;

                            @Override
                            public void onUpdate(final FrameTime frameTime) {
                                super.onUpdate(frameTime);
                                this.setLocalRotation(Quaternion.axisAngle(Vector3.up(), this.theta));
                                this.theta -= frameTime.getDeltaSeconds() * 10.0F;
                            }
                        };
                        node.setRenderable(r);
                        return node;
                    }),
                (room, platter) -> act.map(activity -> {
                    final Scene scene = activity.sceneView.getScene();
                    final Camera camera = scene.getCamera();
//                    final Node light = new Node();
//                    light.setLight(Light.builder(Light.Type.POINT)
//                        .setFalloffRadius(12.0F)
//                        .setIntensity(5000.0F)
//                        .setColor(new Color(1.0F, 1.0F, 1.0F))
//                        .build());
//                    light.setWorldPosition(new Vector3(0.0F, 1.0F, 0.0F));
//                    light.setParent(room);
                    camera.setWorldPosition(new Vector3(0.0F, 1.75F, 1.5F));
                    camera.setLookDirection(Vector3.subtract(new Vector3(0.0F, 0.1F, -0.5F), camera.getWorldPosition()).normalized());
                    room.addChild(platter);
                    scene.addChild(room);
                    final Node platterFloor = new Node();
                    platterFloor.setLocalPosition(new Vector3(0.0F, KartDefinition.inchToMeter(5.0F), -0.15F/*specific to kart model*/));
                    platter.addChild(platterFloor);
                    return platterFloor;
                }).orElse(null))
            .thenCombine(SimpleKartNodeFactory.create(this, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear), (platter, factory) -> {
                act.ifPresent(activity -> {
                    final KartNode kart = factory.create(new KartModel(new SimpleGameModel(), 0, KartDefinition.createKart2()));
                    activity.kart = kart;
                    activity.slider.setProgress(30);
                    platter.addChild(kart);
                });
                return null;
            }).exceptionally(t -> {
                act.ifPresent(activity -> {
                    new AlertDialog.Builder(activity)
                        .setMessage(t.getMessage())
                        .setTitle("Error")
                        .create()
                        .show();
                });
                return null;
            });
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
}