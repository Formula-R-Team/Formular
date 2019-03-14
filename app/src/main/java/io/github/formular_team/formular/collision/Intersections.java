package io.github.formular_team.formular.collision;

import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;

public final class Intersections {
    public static boolean lineCircle(final Vector2 lineStart, final Vector2 lineEnd, final Vector2 circleCenter, final float circleRadius) {
        final Vector2 lineDirection = lineEnd.clone().sub(lineStart);
        final Vector2 delta = lineStart.clone().sub(circleCenter);
        final float a = lineDirection.dot(lineDirection);
        final float b = 2.0F * delta.dot(lineDirection);
        final float c = delta.dot(delta) - circleRadius * circleRadius;
        float discriminant = b * b - 4.0F * a * c;
        if (discriminant < 0.0F) {
            return false;
        }
        discriminant = Mth.sqrt(discriminant);
        final float t0 = (-b - discriminant) / (2.0F * a);
        final float t1 = (-b + discriminant) / (2.0F * a);
        // before     t0 > 1 . t1 > 1    -->|   |
        // enter             . t1 > 1    ---|-> |
        // enter-exit        .           ---|---|-->
        // exit       t0 < 0 .              | --|-->
        // after      t0 < 0 . t1 < 0       |   |-->

        // inside     t0 < 0 . t1 > 1       |-->|
        return t0 >= 0.0F && t0 <= 1.0F || t1 >= 0.0F && t1 <= 1.0F || t0 < 0.0F && t1 > 1.0F;
    }
}
