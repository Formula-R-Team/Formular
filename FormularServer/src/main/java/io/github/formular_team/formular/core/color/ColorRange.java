package io.github.formular_team.formular.core.color;

public interface ColorRange {
    FloatRange red();

    FloatRange green();

    FloatRange blue();

    FloatRange hue();

    FloatRange saturation();

    FloatRange value();

    interface Builder {
        Builder red(final FloatRange red);

        Builder green(final FloatRange green);

        Builder blue(final FloatRange blue);

        Builder hue(final FloatRange hue);

        Builder saturation(final FloatRange saturation);

        Builder value(final FloatRange value);

        ColorRange build();
    }
}
