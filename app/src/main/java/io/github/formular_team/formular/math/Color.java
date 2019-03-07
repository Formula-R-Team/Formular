package io.github.formular_team.formular.math;

public class Color {
    public Color() {}

    public Color(int argb) {}

    @Override
    public Color clone() {
        return new Color();
    }

    public Color copy(final Color target) {
        return this;
    }
}
