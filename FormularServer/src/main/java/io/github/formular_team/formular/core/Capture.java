package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.tracing.ImageMap;

public final class Capture {
    private final float range;

    private final int resolution;

    private final ImageMap image;

    public Capture(final float range, final int resolution, final ImageMap image) {
        this.range = range;
        this.resolution = resolution;
        this.image = image;
    }

    public float getRange() {
        return this.range;
    }

    public int getResolution() {
        return this.resolution;
    }

    public ImageMap getImage() {
        return this.image;
    }
}
