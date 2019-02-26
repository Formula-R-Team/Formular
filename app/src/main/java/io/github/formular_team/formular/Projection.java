package io.github.formular_team.formular;

import android.util.Log;

import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Ray;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;

public final class Projection {
    private Projection() {}

    public static Matrix4 projection(final Matrix4 model, final Matrix4 view, final Matrix4 projection) {
        return model.copy().multiply(view).multiply(projection);
    }

    public static Vector2 project(final Matrix4 mvp, final Vector2 viewport, final Vector3 point) {
        final Vector3 coords = point.copy().applyMatrix4(mvp);
        return new Vector2((coords.x() + 1.0F) / 2.0F * viewport.width(), (1.0F - coords.y()) / 2.0F * viewport.height());
    }

    public static Ray unproject(final Vector2 point, final Matrix4 projectionMatrix, final Matrix4 viewMatrix) {
        final Matrix4 viewProjMtx = projectionMatrix.copy().multiply(viewMatrix);
        return unproject(point, viewProjMtx);
    }

    // final float x = point.x() / viewport.width() * 2.0F - 1.0F;
    // final float y = 1.0F - viewport.height() / viewport.y() * 2.0F;
    public static Ray unproject(final Vector2 point, final Matrix4 viewProjMtx) {
        final Vector3 farScreenPoint = new Vector3(point.x(), point.y(), 1.0F);
        final Vector3 nearScreenPoint = new Vector3(point.x(), point.y(), -1.0F);
        final Matrix4 invertedProjectionMatrix = new Matrix4().getInverse(viewProjMtx, true);
        final Vector3 origin = nearScreenPoint.copy().applyMatrix4(invertedProjectionMatrix);
        final Vector3 direction = farScreenPoint.copy().applyMatrix4(invertedProjectionMatrix);
        test();
        return new Ray(origin, direction.sub(origin).normalize());
    }

    public static void test() {
        final Matrix4 viewProjMtx = new Matrix4();
        viewProjMtx.makePerspective(-1.0F, 1.0F, -1.0F, 1.0F, 0.2F, 10.0F);
        final Vector3 test = new Vector3(0.25F, 0.0F, 0.75F).applyMatrix4(viewProjMtx);
        final Matrix4 invertedProjectionMatrix = new Matrix4().getInverse(viewProjMtx, true);
        Log.i("waldo", "test inverse " + test.applyMatrix4(invertedProjectionMatrix));
    }
}