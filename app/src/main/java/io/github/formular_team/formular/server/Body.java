package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Vector2;

public class Body {
    private final float radius;

    private final float mass;

    private final float inertia;

    private final Matrix3 transform = new Matrix3();

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
        final Vector2 center = new Vector2();
        center.applyMatrix3(this.transform);
        return center;
    }

    public void applyImpulse(final Vector2 force, final Vector2 point) {
        this.applyImpulse(force);
        final Vector2 moment = point.copy();
        moment.sub(this.center());
        this.applyImpulse(moment.cross(force));
    }

    public void applyImpulse(final Vector2 impulse) {
        final Vector2 v = impulse.copy();
        v.multiplyScalar(1.0F / this.mass);
        this.velocity.add(v);
    }

    public void applyImpulse(final float impulse) {
        this.angularVelocity += 1.0F / this.inertia * impulse;
    }

    public void applyForce(final Vector2 force, final Vector2 point) {
        this.applyForce(force);
        final Vector2 moment = point.copy();
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
        final Matrix3 m = new Matrix3();
        this.force.multiplyScalar(delta);
        this.velocity.add(this.force);
        final Vector2 stepVelocity = this.velocity.copy();
        stepVelocity.multiplyScalar(delta);
        m.set(
            1.0F, 0.0F, stepVelocity.x(),
            0.0F, 1.0F, stepVelocity.y(),
            0.0F, 0.0F, 1.0F
        );
        this.transform.multiply(m);
        this.force.setScalar(0.0F);
        this.angularVelocity += this.torque * delta;
        m.set(
            (float) Math.cos(this.angularVelocity * delta), (float) -Math.sin(this.angularVelocity * delta), 0.0F,
            (float) Math.sin(this.angularVelocity * delta), (float) Math.cos(this.angularVelocity * delta), 0.0F,
            0.0F, 0.0F, 1.0F
        );
        this.transform.multiply(m);
        this.torque = 0.0F;
    }
}
