package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Vector2;

public interface Kart {
    KartDefinition getDefinition();

    int getUniqueId();

    void setPosition(final Vector2 position);

    Vector2 getPosition();

    void setRotation(final float rotation);

    float getRotation();

    float getWheelAngularVelocity();

    ControlState getControlState();

    interface ControlState {
        void setThrottle(final float throttle);

        float getThrottle();

        void setBrake(final float brake);

        float getBrake();

        void setSteeringAngle(final float steeringAngle);

        float getSteeringAngle();
    }
}
