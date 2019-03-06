package io.github.formular_team.formular.math;

import java.util.ArrayList;

public interface PathVisitor
{

    ArrayList<Curve> getCurves();

    PathVisitor moveTo(final Vector2 point);

    PathVisitor lineTo(final Vector2 point);

    PathVisitor bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point);

    PathVisitor closePath();
}
