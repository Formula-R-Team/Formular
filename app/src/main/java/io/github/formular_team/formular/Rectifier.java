package io.github.formular_team.formular;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.opengl.Matrix;

import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;

import java.io.Closeable;

public class Rectifier implements Closeable  {
    private final Frame frame;

    private final Image capture;

    private final float[] viewProjMat = new float[16];

    private final float[] mvp = new float[16];

    public Rectifier(final Frame frame) throws NotYetAvailableException {
        this.frame = frame;
        this.capture = frame.acquireCameraImage();
        final Camera camera = frame.getCamera();
        final float[] projMat = new float[16];
        camera.getProjectionMatrix(projMat, 0, 0.1F, 10.0F);
        final float[] viewMat = new float[16];
        camera.getViewMatrix(viewMat, 0);
        Matrix.multiplyMM(this.viewProjMat, 0, projMat, 0, viewMat, 0);
    }

    public Bitmap rectify(final Pose model, final int resolution, final float range) {
        final float[] modelMat = new float[16];
        model.toMatrix(modelMat, 0);
        Matrix.multiplyMM(this.mvp, 0, this.viewProjMat, 0, modelMat, 0);
        final float[] world = {
            -range, 0.0F,  range, 1.0F,
            -range, 0.0F, -range, 1.0F,
             range, 0.0F,  range, 1.0F,
             range, 0.0F, -range, 1.0F
        };
        final float[] pixel = new float[2 * 4];
        this.transform(world, pixel);
        final RectF rect = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < 4; i++) {
            rect.union(pixel[0 + 2 * i], pixel[1 + 2 * i]);
        }
        final Rect r = new Rect();
        rect.roundOut(r);
        r.intersect(new Rect(0, 0, this.capture.getWidth() - 1, this.capture.getHeight() - 1));
        final Bitmap image = Images.yuvToBitmap(this.capture, r);
        return null;
    }

    private void transform(final float[] worldIn, final float[] pixelOut) {
        final int count = worldIn.length / 4;
        final float[] ndcIn = new float[2 * count];
        final float[] vec = new float[4];
        for (int i = 0; i < count; i++) {
            Matrix.multiplyMV(vec, 0, this.mvp, 0, worldIn, 4 * i);
            final float d = 1.0F / vec[3];
            ndcIn[0 + 2 * i] = vec[0] * d;
            ndcIn[1 + 2 * i] = vec[1] * d;
        }
        this.frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, ndcIn,
            Coordinates2d.IMAGE_PIXELS, pixelOut
        );
    }

    @Override
    public void close() {
        this.capture.close();
    }
}
