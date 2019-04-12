package io.github.formular_team.formular.core.color;

public final class FloatRange {
    private final float lowerBound;

    private final float upperBound;

    public FloatRange(final float lowerBound, final float upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static FloatRange create(final float lowerBound, final float upperBound) {
        return new FloatRange(lowerBound, upperBound);
    }

    public float getLowerBound() {
        return this.lowerBound;
    }

    public float getUpperBound() {
        return this.upperBound;
    }
}
