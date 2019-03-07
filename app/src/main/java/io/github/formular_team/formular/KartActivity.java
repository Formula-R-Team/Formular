package io.github.formular_team.formular;

import android.animation.TimeAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class KartActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final int TIMER_MSEC = 30;
    ModelRenderable kartBody, kartTire;
    ArFragment arFragment;
    TransformableNode controlPot;
    Node tireFrontLeft, tireFrontRight, tireBackLeft, tireBackRight;
    final float tireDiameter = 0.25F;

    private boolean isDown = false; //determines if go button is being pressed down
    private boolean isLeftDown = false; // determines if left button is being pressed down
    private boolean isRightDown = false; // determines if right button is being pressed down

    private TimeAnimator mTimer;
    private long mLastTime;



    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kart);

        Button gasBtn = (Button) findViewById(R.id.goBtn);
        gasBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDown = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isDown = false;
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        isDown = false;
                        return true;
                }
                return false;
            }
        });

        Button leftBtn = (Button) findViewById(R.id.leftBtn);
        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLeftDown = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isLeftDown = false;
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        isLeftDown = false;
                        return true;
                }
                return false;
            }
        });

        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isRightDown = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isRightDown = false;
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        isRightDown = false;
                        return true;
                }
                return false;
            }
        });

        mTimer = new TimeAnimator();
        mLastTime = System.currentTimeMillis();
        mTimer.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {

//                long now = System.currentTimeMillis();
//                if((now - mLastTime) < TIMER_MSEC){
//                    return;
//                }
//                mLastTime = now;
                float delta = deltaTime / 1000.0f;
                if(isDown){
                    movePot(delta);
                }
                if(isLeftDown){
                    turnLeft(delta);
                }
                if(isRightDown){
                    turnRight(delta);
                }
            }
        });

        mTimer.start();


        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);

        final float tireWidth = 0.1F;
        final Vector3 kartDim = new Vector3(0.5F, 0.3F, 1.0F);
        final float kartLift = 0.15F;
        final Vector3 tirePos = new Vector3(kartDim.x / 2 + tireWidth * 0.5F, tireDiameter * 0.5F, 0.4F * kartDim.z);
        MaterialFactory.makeOpaqueWithColor(this, new Color(0x0F42DA))
                .thenAccept(bodyMat -> kartBody = ShapeFactory.makeCube(kartDim, new Vector3(0.0F, kartDim.y - kartLift, 0.0F), bodyMat));
        MaterialFactory.makeOpaqueWithColor(this, new Color(0x03001A))
                .thenAccept(tireMat -> kartTire = ShapeFactory.makeCube(new Vector3(tireWidth, tireDiameter, tireDiameter), new Vector3(0.0F, 0.0F, 0.0F), tireMat));

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



        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    if (kartBody == null){
                        return;
                    }

                    Anchor anchor = hitresult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode kart = new TransformableNode(arFragment.getTransformationSystem());

                    kart.getScaleController().setMaxScale(1.0f);
                    kart.getScaleController().setMinScale(0.05f);
                    kart.setLocalScale(new Vector3(0.15f, 0.15f, 0.15f));
                    kart.setParent(anchorNode);
                    kart.setRenderable(kartBody);
                    tireFrontLeft = new Node();
                    tireFrontLeft.setLocalPosition(new Vector3(-tirePos.x, tirePos.y, -tirePos.z));
                    tireFrontLeft.setParent(kart);
                    tireFrontLeft.setRenderable(kartTire);
                    tireFrontRight = new Node();
                    tireFrontRight.setLocalPosition(new Vector3(tirePos.x, tirePos.y, -tirePos.z));
                    tireFrontRight.setLocalRotation(Quaternion.axisAngle(Vector3.up(), (float) Math.PI));
                    tireFrontRight.setParent(kart);
                    tireFrontRight.setRenderable(kartTire);
                    tireBackLeft = new Node();
                    tireBackLeft.setLocalPosition(new Vector3(-tirePos.x, tirePos.y, tirePos.z));
                    tireBackLeft.setParent(kart);
                    tireBackLeft.setRenderable(kartTire);
                    tireBackRight = new Node();
                    tireBackRight.setLocalPosition(new Vector3(tirePos.x, tirePos.y, tirePos.z));
                    tireBackRight.setLocalRotation(Quaternion.axisAngle(Vector3.up(), (float) Math.PI));
                    tireBackRight.setParent(kart);
                    tireBackRight.setRenderable(kartTire);
                    controlPot = kart;
                }
        );


    }

    private void turnRight(float delta) {
        rotate(-140.0F * delta);
    }

    private void turnLeft(float delta) {
        rotate(140.0F * delta);
    }

    private void rotate(final float theta) {
        if(controlPot != null){
            Quaternion rotation = controlPot.getLocalRotation();
            Quaternion rotateDelta = Quaternion.axisAngle(Vector3.up(), theta);
            controlPot.setLocalRotation(Quaternion.multiply(rotation, rotateDelta));
        }
    }

    private void movePot(float delta) {
        if(controlPot != null){
            Vector3 curPos = controlPot.getLocalPosition();
            Vector3 move = new Vector3(0.0F, 0.0F, -0.25F * delta);
            Vector3 rm = Quaternion.rotateVector(controlPot.getLocalRotation(), move);

            controlPot.setLocalPosition(Vector3.add(curPos, rm));
            final float spin = -rm.length() / ((float) Math.PI * this.tireDiameter) * 2.0F * 360.0F;
            this.applyRotation(tireFrontLeft, Vector3.right(), spin);
            this.applyRotation(tireFrontRight, Vector3.right(), spin);
            this.applyRotation(tireBackLeft, Vector3.right(), spin);
            this.applyRotation(tireBackRight, Vector3.right(), spin);
        }
    }

    private void applyRotation(final Node node, final Vector3 axis, final float theta) {
        this.applyRotation(node, Quaternion.axisAngle(axis, theta));
    }

    private void applyRotation(final Node node, final Quaternion q) {
        node.setLocalRotation(Quaternion.multiply(node.getLocalRotation(), q));
    }

}
