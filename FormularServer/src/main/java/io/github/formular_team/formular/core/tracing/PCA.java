package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Vector2;

public final class PCA {
    public static Ellipse get(final float[] x, final float[] y, final float[] w, final int n) {
        if (n == 0) {
            return new Ellipse(new Vector2(), new Vector2());
        }
        final float a = computeCovariance(x, x, w, n);
        final float b = computeCovariance(x, y, w, n);
        final float d = computeCovariance(y, y, w, n);
        return compute(d, b, b, a);
    }

    // https://www.xarg.org/2018/04/how-to-plot-a-covariance-error-ellipse/
    private static Ellipse compute(final float a, final float b, final float c, final float d) {
        if (c == 0.0F) {
            return new Ellipse(new Vector2(), new Vector2());
        }
        final float tmp = Mth.sqrt((a - d) * (a - d) + 4.0F * b * c);
        return new Ellipse(
            new Vector2(1.0F, (tmp + a - d) / (2.0F * c)).setLength(Mth.sqrt((a + d + tmp) / 2.0F)),
            new Vector2(1.0F, -(tmp - a + d) / (2.0F * c)).setLength(Mth.sqrt((a + d - tmp) / 2.0F))
        );
    }

    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online
    private static float computeCovariance(final float[] data1, final float[] data2, final float[] data3, final int n) {
        double meanX = 0.0D, meanY = 0.0D;
        double sumW = 0.0D, sqSumW = 0.0D;
        double C = 0.0D;
        for (int i = 0; i < n; i++) {
            final double x = data1[i];
            final double y = data2[i];
            final double w = data3[i];
            sumW += w;
            sqSumW += w * w;
            final double dx = x - meanX;
            meanX += (w / sumW) * dx;
            meanY += (w / sumW) * (y - meanY);
            C += w * dx * (y - meanY);
        }
        return (float) (C / (sumW - sqSumW / sumW));
    }

    public static final class Ellipse {
        private final Vector2 majorAxis;

        private final Vector2 minorAxis;

        private Ellipse(final Vector2 majorAxis, final Vector2 minorAxis) {
            this.majorAxis = majorAxis;
            this.minorAxis = minorAxis;
        }

        public float getAngle() {
            return this.majorAxis.angle();
        }
    }
}
