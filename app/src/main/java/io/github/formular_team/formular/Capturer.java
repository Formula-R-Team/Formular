package io.github.formular_team.formular;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Matrix;

import java.io.Closeable;

import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.Capture;

public final class Capturer implements Closeable {
    private final Rectifier rectifier;

    public Capturer(final Rectifier rectifier) {
        this.rectifier = rectifier;
    }

    public Capture capture(final Pose pose, final float range, final int resolution) {
        final Matrix model = new Matrix();
        pose.toMatrix(model.data, 0);
        final Matrix rangeScale = new Matrix();
        rangeScale.makeScale(range);
        Matrix.multiply(model, rangeScale, model);
        return new Capture(range, resolution, new BitmapImageMap(this.rectifier.rectify(model.data, resolution)));
    }

    @Override
    public void close() {
        this.rectifier.close();
    }
}
