package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.math.Vector2;

public interface Kart {
    KartDefinition getDefinition();

    int getUniqueId();

    void setPosition(final Vector2 position);

    Vector2 getPosition();

    void setRotation(final float rotation);

    float getRotation();

    float getWheelAngularVelocity();

    ControlState getControlState();

    void setColor(Color color);

    Color getColor();

    interface ControlState {
        ControlState copy(final ControlState other);

        void setThrottle(final float throttle);

        float getThrottle();

        void setBrake(final float brake);

        float getBrake();

        void setSteeringAngle(final float steeringAngle);

        float getSteeringAngle();
    }
}
