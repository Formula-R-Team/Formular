package io.github.formular_team.formular.ar;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;

import io.github.formular_team.formular.core.KartView;
import io.github.formular_team.formular.core.math.Mth;

public class KartNode extends Node {
    private final KartView kart;

    private final Node pivot;

    private final Vector3 localPosition = new Vector3();

    private final Quaternion localRotation = new Quaternion();

    private final ModelRenderable body;

    private KartNode(final KartView kart, final Node pivot, final ModelRenderable body) {
        this.kart = kart;
        this.pivot = pivot;
        this.body = body;
    }

    public void setColor(final Color color) {
        // TODO: strategy color name
        this.body.getMaterial().setFloat4("primaryColor", color);
    }

    @Override
    public void onUpdate(final FrameTime frameTime) {
        super.onUpdate(frameTime);
        this.localPosition.set(this.kart.getPosition().getX(), 0.0F, this.kart.getPosition().getY());
        this.localRotation.set(Vector3.up(), Mth.toDegrees(this.kart.getRotation()));
        this.setLocalPosition(this.localPosition);
        this.pivot.setLocalRotation(this.localRotation);
    }

    private class WheelNode extends Node {
        private final Quaternion initialRotation;

        private float angle = 0.0F;

        final Quaternion localRotation = new Quaternion();

        private WheelNode(final Quaternion initialRotation) {
            this.initialRotation = initialRotation;
        }

        @Override
        public void onUpdate(final FrameTime frameTime) {
            super.onUpdate(frameTime);
            // TODO: kart model holds wheel rotation
            this.angle += KartNode.this.kart.getWheelAngularVelocity();
            this.localRotation.set(Vector3.right(), this.angle);
            this.localRotation.set(Quaternion.multiply(this.localRotation, this.initialRotation));
            this.amendLocal(frameTime);
            this.setLocalRotation(this.localRotation);
        }

        protected void amendLocal(final FrameTime frameTime) {}
    }

    private class SteerWheelNode extends WheelNode {
        private final Quaternion steer = new Quaternion();

        private SteerWheelNode(final Quaternion initialRotation) {
            super(initialRotation);
        }

        @Override
        protected void amendLocal(final FrameTime frameTime) {
            super.amendLocal(frameTime);
            this.steer.set(Vector3.up(), Mth.toDegrees(KartNode.this.kart.getControlState().getSteeringAngle()));
            this.localRotation.set(Quaternion.multiply(this.steer, this.localRotation));
        }
    }

    public static KartNode create(final KartView kart, final ModelRenderable bodyModel, final ModelRenderable wheelFront, final ModelRenderable wheelRear) {
        final float x = 0.5F * kart.getDefinition().width;
        final float z = 0.5F * kart.getDefinition().wheelbase;
        final Node pivotRoot = new Node();
        final KartNode root = new KartNode(kart, pivotRoot, bodyModel);
        pivotRoot.setParent(root);
        final Node body = new Node();
        body.setRenderable(bodyModel);
        body.setParent(pivotRoot);
        final WheelNode frontLeft = root.new SteerWheelNode(Quaternion.axisAngle(Vector3.up(), 180.0F));
        frontLeft.setRenderable(wheelFront);
        frontLeft.setLocalPosition(new Vector3(-x, kart.getDefinition().frontWheelRadius, z));
        frontLeft.setParent(pivotRoot);
        final WheelNode frontRight = root.new SteerWheelNode(Quaternion.identity());
        frontRight.setRenderable(wheelFront);
        frontRight.setLocalPosition(new Vector3(x, kart.getDefinition().frontWheelRadius, z));
        frontRight.setParent(pivotRoot);
        final WheelNode rearLeft = root.new WheelNode(Quaternion.axisAngle(Vector3.up(), 180.0F));
        rearLeft.setRenderable(wheelRear);
        rearLeft.setLocalPosition(new Vector3(-x, kart.getDefinition().rearWheelRadius, -z));
        rearLeft.setParent(pivotRoot);
        final WheelNode rearRight = root.new WheelNode(Quaternion.identity());
        rearRight.setRenderable(wheelRear);
        rearRight.setLocalPosition(new Vector3(x, kart.getDefinition().rearWheelRadius, -z));
        rearRight.setParent(pivotRoot);
        return root;
    }
}
