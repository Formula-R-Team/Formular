package io.github.formular_team.formular;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import io.github.formular_team.formular.car.Kart;
import io.github.formular_team.formular.car.KartDefinition;
import io.github.formular_team.formular.math.Mth;

public class KartActivity extends AppCompatActivity {
    ModelRenderable kartBody, kartWheel;

    ArFragment arFragment;

    private Kart kart;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_kart);

        final View joystick = this.findViewById(R.id.joystick);
        joystick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // TODO: flip x/y if landscape orientation
                final float x = Mth.clamp(event.getX() / v.getWidth() * 2.0F - 1.0F, -1.0F, 1.0F);
                final float y = Mth.clamp(event.getY() / v.getHeight() * 2.0F - 1.0F, -1.0F, 1.0F);
                if (this.kart != null) {
                    this.kart.steerangle = -Mth.PI / 4.0F * x;
                    this.kart.throttle = Math.max(-y, 0.0F) * 50;
                    this.kart.brake = Math.max(y, 0.0F) * 100;
                }
            case MotionEvent.ACTION_UP:
                return true;
            }
            return false;
        });

        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);

        ModelRenderable.builder()
            .setSource(this, Uri.parse("kart.sfb"))
            .build()
            .thenAccept(renderable -> this.kartBody = renderable)
            .exceptionally(throwable -> {
                final Toast toast = Toast.makeText(this, "Unable to load kart", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return null;
            });
        ModelRenderable.builder()
            .setSource(this, Uri.parse("wheel.sfb"))
            .build()
            .thenAccept(renderable -> this.kartWheel = renderable)
            .exceptionally(throwable -> {
                final Toast toast = Toast.makeText(this, "Unable to load wheel", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return null;
            });

        final Scene scene = this.arFragment.getArSceneView().getScene();
//        scene.addOnUpdateListener(frameTime -> {
//            if (this.controller != null) {
//                this.controller.step(frameTime.getDeltaSeconds());
//            }
//        });
        this.arFragment.setOnTapArPlaneListener((HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
            if (this.kartBody == null || this.kartWheel == null){
                return;
            }
            final Anchor anchor = hitresult.createAnchor();
            final AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setLocalScale(Vector3.one().scaled(0.04F));
            anchorNode.setParent(scene);

            final KartDefinition definition = new KartDefinition();
            final float hack = 2.5375F;
            definition.wheelbase = 1.982F * hack;
            final float t = 0.477F;
            definition.b = (1.0F - t) * definition.wheelbase;
            definition.c = t * definition.wheelbase;
            definition.h = 0.7F;
            definition.mass = 1500.0F;
            definition.inertia = 1500.0F;
            definition.width = 1.1176F * hack;
            definition.length = 2.794F * hack;
            definition.wheelradius = 0.248F * hack;
            definition.tireGrip = 2.0F;
            definition.caF = -5.0F;
            definition.caR = -5.2F;
            this.kart = new Kart(definition);
            KartNode.create(this.kart, this.kartBody, this.kartWheel).setParent(anchorNode);
        });
    }
}
