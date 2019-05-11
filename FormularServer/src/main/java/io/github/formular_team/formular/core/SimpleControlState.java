package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.kart.Kart;

public final class SimpleControlState implements Kart.ControlState {
    private float throttle;

    private float brake;

    private float steeringAngle;

    @Override
    public SimpleControlState copy(final Kart.ControlState other) {
        this.setThrottle(other.getThrottle());
        this.setBrake(other.getBrake());
        this.setSteeringAngle(other.getSteeringAngle());
        return this;
    }

    @Override
    public void setThrottle(final float throttle) {
        this.throttle = throttle;
    }

    @Override
    public float getThrottle() {
        return this.throttle;
    }

    @Override
    public void setBrake(final float brake) {
        this.brake = brake;
    }

    @Override
    public float getBrake() {
        return this.brake;
    }

    @Override
    public void setSteeringAngle(final float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    @Override
    public float getSteeringAngle() {
        return this.steeringAngle;
    }
}
