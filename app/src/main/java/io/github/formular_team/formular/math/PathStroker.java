package io.github.formular_team.formular.math;

import java.util.ArrayList;
import java.util.List;

public final class PathStroker {
    public static Shape stroke(final Curve path, final int steps, final float width) {
        final Shape shape = new Shape();
        final Vector2 point = new Vector2();
        final List<Vector2> vertices = new ArrayList<>(steps);
        final List<Vector2> normals = new ArrayList<>(steps);
        for (int n = 0; n <= steps; n++) {
            final float u = n / (float) steps;
            vertices.add(path.getPointAt(u));
            normals.add(path.getTangentAt(u).rotateAround(new Vector2(), 0.5F * Mth.PI));
        }
        for (int n = 0; n <= steps; n++) {
            final Vector2 normal = normals.get(n);
            point.copy(normal);
            point.multiply(0.5F * width);
            point.add(vertices.get(n));
            if (n == 0) {
                shape.moveTo(point.getX(), point.getY());
            } else {
                shape.lineTo(point.getX(), point.getY());
            }
        }
        for (int n = 0; n <= steps; n++) {
            final Vector2 normal = normals.get(n);
            point.copy(normal);
            point.multiply(-0.5F * width);
            point.add(vertices.get(n));
            if (n == 0) {
                shape.moveTo(point.getX(), point.getY());
            } else {
                shape.lineTo(point.getX(), point.getY());
            }
        }
        return shape;
    }
}
