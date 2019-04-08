package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;

public class CirclePathLocator implements PathLocator {
    private final float radius;

    public CirclePathLocator(final float radius) {
        this.radius = radius;
    }

    @Override
    public Vector2 locate(final Mapper map) {
        final Vector2 pos = new Vector2();
        float maxStrength = Float.NEGATIVE_INFINITY;
        final int circum = (int) (Mth.TAU * this.radius);
        final float delta = Mth.TAU / circum;
        for (int n = 0; n < circum; n++) {
            final float theta = n * delta;
            final float x = this.radius * Mth.cos(theta);
            final float y = this.radius * Mth.sin(theta);
            final float strength = map.get(x, y);
            if (strength > maxStrength) {
                pos.set(x, y);
                maxStrength = strength;
            }
        }
        return pos;
    }
}
