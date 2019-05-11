package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.kart.KartDefinition;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.kart.KartView;
import io.github.formular_team.formular.core.math.Vector2;

public class DirectKartView implements KartView {
    private final KartModel kart;

    public DirectKartView(final KartModel kart) {
        this.kart = kart;
    }

    @Override
    public KartDefinition getDefinition() {
        return this.kart.getDefinition();
    }

    @Override
    public int getUniqueId() {
        return this.kart.getUniqueId();
    }

    @Override
    public void setPosition(final Vector2 position) {
        this.kart.setPosition(position);
    }

    @Override
    public Vector2 getPosition() {
        return this.kart.getPosition();
    }

    @Override
    public void setRotation(final float rotation) {
        this.kart.setRotation(rotation);
    }

    @Override
    public float getRotation() {
        return this.kart.getRotation();
    }

    @Override
    public float getWheelAngularVelocity() {
        return this.kart.getWheelAngularVelocity();
    }

    @Override
    public ControlState getControlState() {
        return this.kart.getControlState();
    }

    @Override
    public void setColor(final Color color) {
        this.kart.setColor(color);
    }

    @Override
    public Color getColor() {
        return this.kart.getColor();
    }
}
