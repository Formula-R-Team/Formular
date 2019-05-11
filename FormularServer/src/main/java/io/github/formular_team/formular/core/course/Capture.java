package io.github.formular_team.formular.core.course;

import io.github.formular_team.formular.core.tracing.ImageMap;

public final class Capture {
    private final float radius;

    private final int resolution;

    private final ImageMap image;

    public Capture(final float radius, final int resolution, final ImageMap image) {
        this.radius = radius;
        this.resolution = resolution;
        this.image = image;
    }

    public float getRadius() {
        return this.radius;
    }

    public int getResolution() {
        return this.resolution;
    }

    public ImageMap getImage() {
        return this.image;
    }
}
