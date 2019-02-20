package io.github.formular_team.formular.server;

public final class SimpleKart extends Body implements Kart {
    private final KartDefinition definition;

    private float throttle;

    private float brake;

    private float steerAngle;

    private float speed;

    private SimpleKart(final KartDefinition definition) {
        super(1.0F, 5.0F, 0.75F);
        this.definition = definition;
    }

    @Override
    public void shift(final Gear position) {

    }

    @Override
    public void step(final float delta) {
    }
}
