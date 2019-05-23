package io.github.formular_team.formular.core.course;

import java.util.List;

import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.tracing.PCA;

public final class PathPose {
    private final float scale;

    private final Vector2 position;

    private final PCA.Ellipse ellipse;

    public PathPose(final float scale, final Vector2 position, final PCA.Ellipse ellipse) {
        this.scale = scale;
        this.position = position;
        this.ellipse = ellipse;
    }

    public float getScale() {
        return this.scale;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public PCA.Ellipse getEllipse() {
        return this.ellipse;
    }

    public static PathPose get(final Path path, final float scale) {
        final List<Vector2> points = path.getPoints(false);
        final int n = points.size();
        final float[] x = new float[n], y = new float[n], w = new float[n];
        final Vector2 avg = new Vector2();
        for (int i = 0; i < n ; i++) {
            final Vector2 point = points.get(i);
            x[i] = point.getX();
            y[i] = point.getY();
            w[i] = 1.0F;
            avg.add(point);
        }
        avg.divide(n);
        return new PathPose(scale, avg, PCA.get(x, y, w, n));
    }
}
