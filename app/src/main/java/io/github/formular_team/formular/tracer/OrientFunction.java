package io.github.formular_team.formular.tracer;

public class OrientFunction implements ToDoubleMapFunction {
    private final int radius;

    private final double[] bufX;

    private final double[] bufY;

    private final double[] bufW;

    public OrientFunction(final int radius) {
        this.radius = radius;
        final int count = (2 * this.radius + 1) * (2 * this.radius + 1);
        this.bufX = new double[count];
        this.bufY = new double[count];
        this.bufW = new double[count];
    }

    @Override
    public double orient(final Mapper image) {
        int n = 0;
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                final double w = image.get(x, y);
                if (w != 0.0D) {
                    this.bufX[n] = x;
                    this.bufY[n] = y;
                    this.bufW[n] = w;
                    n++;
                }
            }
        }
        final double a = cov(this.bufX, this.bufX, this.bufW, n);
        final double b = cov(this.bufX, this.bufY, this.bufW, n);
        final double d = cov(this.bufY, this.bufY, this.bufW, n);
        return this.computeSpreadAngle(d, b, a);
    }

    private double computeSpreadAngle(final double a, final double b, final double d) {
        return Math.atan2(b == 0.0D ? 0.0D : (Math.sqrt((a - d) * (a - d) + 4.0D * b * b) + a - d) / (2.0D * b), 1.0D);
    }

    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online
    private static double cov(final double[] data1, final double[] data2, final double[] data3, final int n) {
        double meanx = 0.0D, meany = 0.0D;
        double wsum = 0.0D, wsum2 = 0.0D;
        double C = 0.0D;
        for (int i = 0; i < n; i++) {
            final double x = data1[i];
            final double y = data2[i];
            final double w = data3[i];
            wsum += w;
            wsum2 += w * w;
            final double dx = x - meanx;
            meanx += (w / wsum) * dx;
            meany += (w / wsum) * (y - meany);
            C += w * dx * (y - meany);
        }
        return C / (wsum - wsum2 / wsum);
    }
}
