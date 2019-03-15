package io.github.formular_team.formular.collision;

import io.github.formular_team.formular.math.Box2;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;

public final class Intersections {
    // TODO intersection point result
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

    // https://www.geeksforgeeks.org/program-for-point-of-intersection-of-two-lines/
    public static boolean lineLine(final Vector2 A, final Vector2 B, final Vector2 C, final Vector2 D, final Vector2 result) {
        final float a1 = B.getY() - A.getY();
        final float b1 = A.getX() - B.getX();
        final float c1 = a1 * A.getX() + b1 * A.getY();
        final float a2 = D.getY() - C.getY();
        final float b2 = C.getX() - D.getX();
        final float c2 = a2 * C.getX() + b2 * C.getY();
        final float determinant = a1 * b2 - a2 * b1;
        if (determinant == 0.0F) {
            return false;
        }
        result.set((b2 * c1 - b1 * c2) / determinant, (a1 * c2 - a2 * c1) / determinant);
        final Box2 box = new Box2();
        return box.setFromPoints(A, B).isContainsPoint(result) && box.setFromPoints(C, D).isContainsPoint(result);
    }
}
