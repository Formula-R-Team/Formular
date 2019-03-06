package io.github.formular_team.formular.math;

public class Color {
    public Color() {}

    public Color(int argb) {}

    public Color copy() {
        return new Color();
    }

    public Color copy(final Color target) {
        return this;
    }
}
