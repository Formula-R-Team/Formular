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

    private final Rect wholeCapture;

    private final float[] viewProjMat = new float[16];

    private final float[] mvp = new float[16];

    public Rectifier(final Frame frame) throws NotYetAvailableException {
        this.frame = frame;
        this.capture = frame.acquireCameraImage();
        this.wholeCapture = new Rect(0, 0, this.capture.getWidth(), this.capture.getHeight());
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
        final int count = resolution * resolution;
        final int buflen = Math.max(4, count);
        final float[] in = new float[4 * buflen];
        final float[] ndc = new float[2 * buflen];
        final float[] out = new float[2 * buflen];
        setVec4(in, 0, -range, 0.0F, range, 1.0F);
        setVec4(in, 1, -range, 0.0F, -range, 1.0F);
        setVec4(in, 2, range, 0.0F, range, 1.0F);
        setVec4(in, 3, range, 0.0F, -range, 1.0F);
        this.transform(in, ndc, out, 4);
        final RectF captureBounds = new RectF(
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY
        );
        for (int i = 1; i < 4; i++) {
            captureBounds.union(getVec2X(out, i), getVec2Y(out, i));
        }
        final Rect captureRegion = new Rect();
        captureBounds.roundOut(captureRegion);
        if (!captureRegion.intersect(this.wholeCapture)) {
            return null;
        }
        final Bitmap subcapture = Images.yuvToBitmap(this.capture, captureRegion);
        if (subcapture == null) {
            return null;
        }
        final Bitmap rectified = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                setVec4(in,
                    x + y * resolution,
                    range * (2.0F * x / resolution - 1.0F),
                    0.0F,
                    -range * (2.0F * y / resolution - 1.0F),
                    1.0F
                );
            }
        }
        this.transform(in, ndc, out, count);
        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                final int i = x + y * resolution;
                final int captureX = (int) (getVec2X(out, i) - captureRegion.left);
                final int captureY = (int) (getVec2Y(out, i) - captureRegion.top);
                if (captureRegion.contains(captureX, captureY)) {
                    rectified.setPixel(x, y, subcapture.getPixel(captureX, captureY));
                }
            }
        }
        return rectified;
    }

    private void transform(final float[] modelspace, final float[] ndc, final float[] pixelspace, final int count) {
        final float[] vec = new float[4];
        for (int i = 0; i < count; i++) {
            Matrix.multiplyMV(vec, 0, this.mvp, 0, modelspace, 4 * i);
            final float d = 1.0F / vec[3];
            setVec2(ndc, i, vec[0] * d, vec[1] * d);
        }
        this.frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, ndc,
            Coordinates2d.IMAGE_PIXELS, pixelspace
        );
    }

    @Override
    public void close() {
        this.capture.close();
    }

    private static void setVec4(final float[] buf, final int index, final float x, final float y, final float z, final float w) {
        buf[0 + 4 * index] = x;
        buf[1 + 4 * index] = y;
        buf[2 + 4 * index] = z;
        buf[3 + 4 * index] = w;
    }

    private static void setVec2(final float[] buf, final int index, final float x, final float y) {
        buf[0 + 2 * index] = x;
        buf[1 + 2 * index] = y;
    }

    private static float getVec2X(final float[] buf, final int index) {
        return buf[0 + 2 * index];
    }

    private static float getVec2Y(final float[] buf, final int index) {
        return buf[1 + 2 * index];
    }
}
