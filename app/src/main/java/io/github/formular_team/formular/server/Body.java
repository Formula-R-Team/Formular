package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Vector2;

public class Body {
    private final float radius;

    private final float mass;

    private final float inertia;

    private float transformX, transformY, transformAngle;

    private float angularVelocity = 0.0F;

    private final Vector2 velocity = new Vector2();

    private final Vector2 force = new Vector2();

    private float torque = 0.0F;

    public Body(final float radius, final float mass, final float inertia) {
        this.radius = radius;
        this.mass = mass;
        this.inertia = inertia;
    }

    private Vector2 center() {
        return new Vector2(this.transformX, this.transformY);
    }

    public void applyImpulse(final Vector2 force, final Vector2 point) {
        this.applyImpulse(force);
        final Vector2 moment = point.clone();
        moment.sub(this.center());
        this.applyImpulse(moment.cross(force));
    }

    public void applyImpulse(final Vector2 impulse) {
        final Vector2 v = impulse.clone();
        v.multiply(1.0F / this.mass);
        this.velocity.add(v);
    }

    public void applyImpulse(final float impulse) {
        this.angularVelocity += 1.0F / this.inertia * impulse;
    }

    public void applyForce(final Vector2 force, final Vector2 point) {
        this.applyForce(force);
        final Vector2 moment = point.clone();
        moment.sub(this.center());
        this.applyTorque(moment.cross(force));
    }

    public void applyForce(final Vector2 force) {
        this.force.add(force);
    }

    public void applyTorque(final float torque) {
        this.torque += torque;
    }

    public void step(final float delta) {
        this.angularVelocity += this.torque * delta;
        this.transformAngle += this.angularVelocity * delta;
//        this.transform.rotate(this.angularVelocity * delta);
        this.torque = 0.0F;

        this.velocity.add(this.force.multiply(delta));
        final Vector2 stepVelocity = this.velocity.clone().multiply(delta);
        this.transformX += stepVelocity.getX();
        this.transformY += stepVelocity.getY();
//        this.transform.translate(stepVelocity.getX(), stepVelocity.getY());
        this.force.set(0.0F, 0.0F);

        this.velocity.multiply(0.85F);
        if (this.velocity.length() < 0.0001F) {
            this.velocity.set(0.0F, 0.0F);
        }
        this.angularVelocity *= 0.8F;
        if (this.angularVelocity < 0.0001F) {
            this.angularVelocity = 0.0F;
        }
    }

    public float getTransformX() {
        return this.transformX;
    }

    public float getTransformY() {
        return this.transformY;
    }

    public float getTransformAngle() {
        return this.transformAngle;
    }

    public Matrix3 getTransform() {
        final Matrix3 m = new Matrix3();
        m.translate(this.transformX, this.transformY);
        m.rotate(this.transformAngle);
        return m;
    }
}
