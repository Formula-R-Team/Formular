package io.github.formular_team.formular.ar.scene;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.KartModel;

public class KartNode extends Node {
    private final Kart kart;

    private final Vector3 localPosition = new Vector3();

    private final Quaternion localRotation = new Quaternion();

    private KartNode(final KartModel kart) {
        this.kart = kart;
    }

    @Override
    public void onUpdate(final FrameTime frameTime) {
        super.onUpdate(frameTime);
        this.localPosition.set(this.kart.getPosition().getX(), 0.0F, this.kart.getPosition().getY());
        this.localRotation.set(Vector3.up(), Mth.toDegrees(this.kart.getRotation()));
        this.setLocalPosition(this.localPosition);
        this.setLocalRotation(this.localRotation);
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

    public static KartNode create(final KartModel kart, final ModelRenderable bodyModel, final ModelRenderable wheelModel) {
        final float x = 0.5F * kart.getDefinition().width;
        final float y = kart.getDefinition().wheelradius;
        final float z = 0.5F * kart.getDefinition().wheelbase;
        final KartNode root = new KartNode(kart);
        final Node body = new Node();
        body.setLocalPosition(new Vector3(0.0F, y, 0.0F));
        body.setRenderable(bodyModel);
        body.setParent(root);
        final WheelNode frontLeft = root.new SteerWheelNode(Quaternion.axisAngle(Vector3.up(), 180.0F));
        frontLeft.setRenderable(wheelModel);
        frontLeft.setLocalPosition(new Vector3(-x, y, z));
        frontLeft.setParent(root);
        final WheelNode frontRight = root.new SteerWheelNode(Quaternion.identity());
        frontRight.setRenderable(wheelModel);
        frontRight.setLocalPosition(new Vector3(x, y, z));
        frontRight.setParent(root);
        final WheelNode rearLeft = root.new WheelNode(Quaternion.axisAngle(Vector3.up(), 180.0F));
        rearLeft.setRenderable(wheelModel);
        rearLeft.setLocalPosition(new Vector3(-x, y, -z));
        rearLeft.setParent(root);
        final WheelNode rearRight = root.new WheelNode(Quaternion.identity());
        rearRight.setRenderable(wheelModel);
        rearRight.setLocalPosition(new Vector3(x, y, -z));
        rearRight.setParent(root);
        return root;
    }
}
