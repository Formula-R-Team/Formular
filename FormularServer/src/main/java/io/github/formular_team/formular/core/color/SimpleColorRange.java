package io.github.formular_team.formular.core.color;

import java.util.Objects;

public final class SimpleColorRange implements ColorRange {
    private final FloatRange red;

    private final FloatRange green;

    private final FloatRange blue;

    private final FloatRange hue;

    private final FloatRange saturation;

    private final FloatRange value;

    private SimpleColorRange(final Builder builder) {
        this.red = builder.red;
        this.green = builder.green;
        this.blue = builder.blue;
        this.hue = builder.hue;
        this.saturation = builder.saturation;
        this.value = builder.value;
    }

    @Override
    public FloatRange red() {
        return this.red;
    }

    @Override
    public FloatRange green() {
        return this.green;
    }

    @Override
    public FloatRange blue() {
        return this.blue;
    }

    @Override
    public FloatRange hue() {
        return this.hue;
    }

    @Override
    public FloatRange saturation() {
        return this.saturation;
    }

    @Override
    public FloatRange value() {
        return this.value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder implements ColorRange.Builder {
        private static final FloatRange RANGE_ZERO_ONE = FloatRange.create(0.0F, 1.0F);

        private FloatRange red = RANGE_ZERO_ONE;

        private FloatRange green = RANGE_ZERO_ONE;

        private FloatRange blue = RANGE_ZERO_ONE;

        private FloatRange hue = FloatRange.create(0.0F, 360.0F);

        private FloatRange saturation = RANGE_ZERO_ONE;

        private FloatRange value = RANGE_ZERO_ONE;

        private Builder() {}

        @Override
        public ColorRange.Builder red(final FloatRange red) {
            this.red = Objects.requireNonNull(red);
            return this;
        }

        @Override
        public ColorRange.Builder green(final FloatRange green) {
            this.green = Objects.requireNonNull(green);
            return this;
        }

        @Override
        public ColorRange.Builder blue(final FloatRange blue) {
            this.blue = Objects.requireNonNull(blue);
            return this;
        }

        @Override
        public ColorRange.Builder hue(final FloatRange hue) {
            this.hue = Objects.requireNonNull(hue);
            return this;
        }

        @Override
        public ColorRange.Builder saturation(final FloatRange saturation) {
            this.saturation = Objects.requireNonNull(saturation);
            return this;
        }

        @Override
        public ColorRange.Builder value(final FloatRange value) {
            this.value = Objects.requireNonNull(value);
            return this;
        }

        @Override
        public ColorRange build() {
            return new SimpleColorRange(this);
        }
    }
}
