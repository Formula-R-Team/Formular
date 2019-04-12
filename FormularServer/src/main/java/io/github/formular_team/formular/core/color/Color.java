package io.github.formular_team.formular.core.color;

import io.github.formular_team.formular.core.math.Mth;

public class Color {
    private float red;

    private float green;

    private float blue;

    public Color() {
        this(1.0F, 1.0F, 1.0F);
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    public Color(final float red, final float green, final float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color copy() {
        return new Color().copy(this);
    }

    public Color copy(final Color target) {
        this.red = target.red;
        this.green = target.green;
        this.blue = target.blue;
        return this;
    }

    public static Color hsb(final float hue, final float saturation, final float brightness) {
        final float normalizedHue = (((hue % 360.0F) + 360.0F) % 360) / 360.0F;
        final float h = (normalizedHue - Mth.floor(normalizedHue)) * 6.0F;
        final float f = h - Mth.floor(h);
        final float p = brightness * (1.0F - saturation);
        final float q = brightness * (1.0F - f * saturation);
        final float t = brightness * (1.0F - (1.0F - f) * saturation);
        final float r;
        final float g;
        final float b;
        switch ((int) h) {
        case 0:
            r = brightness;
            g = t;
            b = p;
            break;
        case 1:
            r = q;
            g = brightness;
            b = p;
            break;
        case 2:
            r = p;
            g = brightness;
            b = t;
            break;
        case 3:
            r = p;
            g = q;
            b = brightness;
            break;
        case 4:
            r = t;
            g = p;
            b = brightness;
            break;
        case 5:
            r = brightness;
            g = p;
            b = q;
            break;
        default:
            throw new AssertionError();
        }
        final float red = Mth.clamp(r, 0.0F, 1.0F);
        final float green = Mth.clamp(g, 0.0F, 1.0F);
        final float blue = Mth.clamp(b, 0.0F, 1.0F);
        return new Color(red, green, blue);
    }
}
