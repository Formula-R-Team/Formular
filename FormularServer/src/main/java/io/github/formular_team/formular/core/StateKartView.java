package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.kart.KartDefinition;
import io.github.formular_team.formular.core.kart.KartView;
import io.github.formular_team.formular.core.math.Vector2;

public class StateKartView implements KartView {
    private final int uniqueId;

    private final KartDefinition definition;

    private final ControlState state = new SimpleControlState();

    private final Vector2 position;

    private float rotation;

    private Color color;

    public StateKartView(final int uniqueId, final KartDefinition definition, final Vector2 position, final float rotation) {
        this.uniqueId = uniqueId;
        this.definition = definition;
        this.position = position;
        this.rotation = rotation;
    }

    @Override
    public KartDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public int getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public void setPosition(final Vector2 position) {
        this.position.copy(position);
    }

    @Override
    public Vector2 getPosition() {
        return this.position;
    }

    @Override
    public void setRotation(final float rotation) {
        this.rotation = rotation;
    }

    @Override
    public float getRotation() {
        return this.rotation;
    }

    @Override
    public float getWheelAngularVelocity() {
        return 0.0F;
    }

    @Override
    public ControlState getControlState() {
        return this.state;
    }

    @Override
    public void setColor(final Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return this.color;
    }
}
