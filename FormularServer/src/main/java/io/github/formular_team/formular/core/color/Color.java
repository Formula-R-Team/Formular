package io.github.formular_team.formular.core.color;

import io.github.formular_team.formular.core.math.Mth;

public final class Color {
    private int hex;

    private Color() {
        this(0xFFFFFFFF);
    }

    private Color(final int hex) {
        this.hex = hex;
    }

    public int getHex() {
        return this.hex;
    }

    public float getRed() {
        return (this.hex >> 16 & 0xFF) / 255.0F;
    }

    public float getGreen() {
        return (this.hex >> 8 & 0xFF) / 255.0F;
    }

    public float getBlue() {
        return (this.hex & 0xFF) / 255.0F;
    }

    public float getOpacity() {
        return (this.hex >> 24 & 0xFF) / 255.0F;
    }

    public Color copy() {
        return new Color().copy(this);
    }

    public Color copy(final Color target) {
        this.hex = target.hex;
        return this;
    }

    @Override
    public String toString() {
        return String.format("0x%08x", this.hex);
    }

    public static Color hex(final int hex) {
        return new Color(hex);
    }

    public static Color color(final float red, final float green, final float blue) {
        return Color.color(red, green, blue, 1.0F);
    }

    public static Color color(final float red, final float green, final float blue, final float opacity) {
        return Color.rgb((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), opacity);
    }

    public static Color rgb(final int red, final int green, final int blue) {
        return Color.rgb(red, green, blue, 1.0F);
    }

    public static Color rgb(final int red, final int green, final int blue, final float opacity) {
        return Color.hex((int) (opacity * 255.0F) << 24 | red << 16 | green << 8 | blue);
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
        return Color.color(red, green, blue);
    }
}
