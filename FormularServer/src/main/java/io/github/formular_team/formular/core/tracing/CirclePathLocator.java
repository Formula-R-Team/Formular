package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Vector2;

/**
 * An implementation of a {@link PathLocator} which locates a path that intersects a circle of the specified radius at the origin.
 */
public class CirclePathLocator implements PathLocator {
    private final float radius;

    /**
     * Constructs a {@link CirclePathLocator} with the given radius.
     *
     * @param radius radius of circle
     */
    public CirclePathLocator(final float radius) {
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     */
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
