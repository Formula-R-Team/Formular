package io.github.formular_team.formular;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;

import io.github.formular_team.formular.car.Car;
import io.github.formular_team.formular.car.CarDefinition;
import io.github.formular_team.formular.math.Mth;

public class KartActivity extends AppCompatActivity {
    ModelRenderable kartBody, kartTire;

    ArFragment arFragment;

    final CarDefinition definition = CarDefinition.createDefault();

    final Vector3 kartDim = new Vector3(this.definition.width, 1.0F, this.definition.length);

    final float kartLift = 0.15F;

    final Vector3 tirePos = new Vector3(this.kartDim.x * 0.5F + this.definition.wheelwidth * 0.5F, this.definition.wheellength * 0.5F, 0.4F * this.kartDim.z);

    private CarController controller;

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
                if (this.controller != null) {
                    this.controller.car.steerangle = -Mth.PI / 4.0F * x;
                    this.controller.car.throttle = Math.max(-y, 0.0F) * 50;
                    this.controller.car.brake = Math.max(y, 0.0F) * 100;
                    this.controller.car.tireGrip = 2.0F;
                }
            case MotionEvent.ACTION_UP:
                return true;
            }
            return false;
        });

        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);

        MaterialFactory.makeOpaqueWithColor(this, new Color(0x0F42DA))
            .thenAccept(bodyMat -> this.kartBody = ShapeFactory.makeCube(this.kartDim, new Vector3(0.0F, this.kartDim.y - this.kartLift, 0.0F), bodyMat));
        MaterialFactory.makeOpaqueWithColor(this, new Color(0x03001A))
            .thenAccept(tireMat -> this.kartTire = ShapeFactory.makeCube(new Vector3(this.definition.wheelwidth, this.definition.wheellength, this.definition.wheellength), new Vector3(0.0F, 0.0F, 0.0F), tireMat));

        //        ModelRenderable.builder()
//                .setSource(this, Uri.parse("teapot.sfb"))
//                .build()
//                .thenAccept(renderable -> teaPotRenderable = renderable)
//                .exceptionally(throwable -> {
//                    Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                    return null;
//                });

        final Scene scene = this.arFragment.getArSceneView().getScene();
        scene.addOnUpdateListener(frameTime -> {
            if (this.controller != null) {
                this.controller.step(frameTime.getDeltaSeconds());
            }
        });
        this.arFragment.setOnTapArPlaneListener((HitResult hitresult, Plane plane, MotionEvent motionevent) -> {

            if (this.kartBody == null || this.kartTire == null){
                return;
            }
            final Anchor anchor = hitresult.createAnchor();
            final AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setLocalScale(Vector3.one().scaled(0.04F));
            anchorNode.setParent(scene);

            final Node bodyNode = new Node();
            bodyNode.setParent(anchorNode);
            bodyNode.setRenderable(this.kartBody);
            final Node tireFrontLeft = new Node();
            tireFrontLeft.setLocalPosition(new Vector3(-this.tirePos.x, this.tirePos.y, -this.tirePos.z));
            tireFrontLeft.setParent(bodyNode);
            tireFrontLeft.setRenderable(this.kartTire);
            Wheel frontLeft = new Wheel(tireFrontLeft,0f);

            final Node tireFrontRight = new Node();
            tireFrontRight.setLocalPosition(new Vector3(this.tirePos.x, this.tirePos.y, -this.tirePos.z));
            tireFrontRight.setLocalRotation(Quaternion.axisAngle(Vector3.up(), (float) Math.PI));
            tireFrontRight.setParent(bodyNode);
            tireFrontRight.setRenderable(this.kartTire);
            Wheel frontRight = new Wheel(tireFrontRight,0f);

            final Node tireRearLeft = new Node();
            tireRearLeft.setLocalPosition(new Vector3(-this.tirePos.x, this.tirePos.y, this.tirePos.z));
            tireRearLeft.setParent(bodyNode);
            tireRearLeft.setRenderable(this.kartTire);
            Wheel rearLeft = new Wheel(tireRearLeft,0f);

            final Node tireRearRight = new Node();
            tireRearRight.setLocalPosition(new Vector3(this.tirePos.x, this.tirePos.y, this.tirePos.z));
            tireRearRight.setLocalRotation(Quaternion.axisAngle(Vector3.up(), (float) Math.PI));
            tireRearRight.setParent(bodyNode);
            tireRearRight.setRenderable(this.kartTire);
            Wheel rearRight = new Wheel(tireRearRight,0f);

            this.controller = new CarController(new Car(this.definition), bodyNode, new Wheel[] { frontRight, rearRight, frontLeft, rearLeft});
        });
    }

    class CarController {
        final Car car;

        final Node model;

        final Wheel[] wheels;

        final Vector3 localPosition = new Vector3();

        final Quaternion localRotation = new Quaternion();

        CarController(final Car car, final Node model, final Wheel[] wheels) {
            this.car = car;
            this.model = model;
            this.wheels = wheels;
        }

        void step(final float delta){
            final CarDefinition definition = CarDefinition.createDefault();
            final float targetDt = 0.01F;
            final int steps = Math.max((int) (delta / targetDt), 1);
            final float dt = delta / steps;
            for (int n = 0; n < steps; n++) {
                this.car.step(dt);
            }
            this.localPosition.set(this.car.position.getX(), 0.0F, this.car.position.getY());
            this.localRotation.set(Vector3.up(), Mth.toDegrees(this.car.rotation));
            this.model.setLocalPosition(this.localPosition);
            this.model.setLocalRotation(this.localRotation);

            //how much the wheels turn
            for(int w = 0; w < 4;w++){
                float rotateCalculation = Math.abs(this.car.velocity.getX())/(definition.wheellength/2);
                float wheelRotation = wheels[w].getRotationAngle();
                Quaternion right = new Quaternion(Vector3.right(),wheelRotation + rotateCalculation);
                wheels[w].setRotationAngle(wheelRotation + rotateCalculation);
                wheels[w].getNode().setLocalRotation(right);

                if(w == 1 || w == 3){
                    Quaternion steerangle = new Quaternion(Vector3.up(),Mth.toDegrees(this.car.steerangle));
                    wheels[w].getNode().setLocalRotation(Quaternion.multiply(steerangle,right));
                }

            }
        }


    }

    class Wheel {

        private Node node;

        private float rotationAngle;

        Wheel(Node node, float rotationAngle){
            this.node = node;
            this.rotationAngle = rotationAngle;
        }

        public float getRotationAngle() {
            return rotationAngle;
        }

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public void setRotationAngle(float steerangle){
            this.rotationAngle = steerangle;
        }
    }
}
