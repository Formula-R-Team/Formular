package io.github.formular_team.formular.core.math;

public final class Mth {
    private Mth() {}

    public static final float PI = (float) Math.PI;

    public static final float TAU = 2.0F * Mth.PI;

    public static float toRadians(final float degrees) {
        return degrees / 180.0F * PI;
    }

    public static float toDegrees(final float radians) {
        return radians * 180.0F / PI;
    }

    public static float deltaAngle(final float a, final float b) {
        return deltaMod(a, b, 360.0F);
    }

    public static float deltaMod(final float a, final float b, final float n) {
        return mod(a - b + 0.5F * n, n) - 0.5F * n;
    }

    public static int deltaMod(final int a, final int b, final int n) {
        return Math.floorMod(a - b + n / 2, n) - n / 2;
    }

    public static float mod(final float a, final float b) {
        return (a % b + b) % b;
    }

    public static float sqrt(final float a) {
        return (float) Math.sqrt(a);
    }

    public static float sin(final float a) {
        return (float) Math.sin(a);
    }

    public static float cos(final float a) {
        return (float) Math.cos(a);
    }

    public static float tan(final float a) {
        return (float) Math.tan(a);
    }

    public static float ceil(final float a) {
        return (float) Math.ceil(a);
    }

    public static float floor(final float a) {
        return (float) Math.floor(a);
    }

    public static float round(final float a) {
        return (float) Math.round(a);
    }

    public static float acos(final float a) {
        return (float) Math.acos(a);
    }

    public static float atan(final double v) {
        return (float) Math.atan(v);
    }

    public static float atan2(final float a, final float b) {
        return (float) Math.atan2(a, b);
    }

    public static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }
}
