package io.github.formular_team.formular.color;

import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.formular_team.formular.util.MorePreconditions.checkBounded;

public final class SimpleColorRange implements ColorRange {
    private final Range<Float> red;

    private final Range<Float> green;

    private final Range<Float> blue;

    private final Range<Float> hue;

    private final Range<Float> saturation;

    private final Range<Float> value;

    private SimpleColorRange(final Builder builder) {
        this.red = builder.red;
        this.green = builder.green;
        this.blue = builder.blue;
        this.hue = builder.hue;
        this.saturation = builder.saturation;
        this.value = builder.value;
    }

    @Override
    public Range<Float> red() {
        return this.red;
    }

    @Override
    public Range<Float> green() {
        return this.green;
    }

    @Override
    public Range<Float> blue() {
        return this.blue;
    }

    @Override
    public Range<Float> hue() {
        return this.hue;
    }

    @Override
    public Range<Float> saturation() {
        return this.saturation;
    }

    @Override
    public Range<Float> value() {
        return this.value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder implements ColorRange.Builder {
        private static final Range<Float> RANGE_ZERO_ONE = Range.closed(0.0F, 1.0F);

        private Range<Float> red = RANGE_ZERO_ONE;

        private Range<Float> green = RANGE_ZERO_ONE;

        private Range<Float> blue = RANGE_ZERO_ONE;

        private Range<Float> hue = Range.closedOpen(0.0F, 360.0F);

        private Range<Float> saturation = RANGE_ZERO_ONE;

        private Range<Float> value = RANGE_ZERO_ONE;

        private Builder() {}

        @Override
        public ColorRange.Builder red(final Range<Float> red) {
            checkNotNull(red);
            this.red = checkBounded(red);
            return this;
        }

        @Override
        public ColorRange.Builder green(final Range<Float> green) {
            checkNotNull(green);
            this.green = checkBounded(green);
            return this;
        }

        @Override
        public ColorRange.Builder blue(final Range<Float> blue) {
            checkNotNull(blue);
            this.blue = checkBounded(blue);
            return this;
        }

        @Override
        public ColorRange.Builder hue(final Range<Float> hue) {
            checkNotNull(hue);
            this.hue = checkBounded(hue);
            return this;
        }

        @Override
        public ColorRange.Builder saturation(final Range<Float> saturation) {
            checkNotNull(saturation);
            this.saturation = checkBounded(saturation);
            return this;
        }

        @Override
        public ColorRange.Builder value(final Range<Float> value) {
            checkNotNull(value);
            this.value = checkBounded(value);
            return this;
        }

        @Override
        public ColorRange build() {
            return new SimpleColorRange(this);
        }
    }
}
