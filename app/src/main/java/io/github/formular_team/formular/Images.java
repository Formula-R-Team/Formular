package io.github.formular_team.formular;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public final class Images {
    private Images() {}

    public static Bitmap yuvToBitmap(final Image image) {
        return yuvToBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()));
    }

    public static Bitmap yuvToBitmap(final Image image, final Rect rect) {
        Preconditions.checkArgument(image.getFormat() == ImageFormat.YUV_420_888);
        if (rect.isEmpty()) {
            return null;
        }
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        try (final WritableByteChannel channel = Channels.newChannel(bout)) {
            for (final int i : new int[] { 0, 2, 1 }) {
                channel.write(image.getPlanes()[i].getBuffer());
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        final YuvImage yuv = new YuvImage(bout.toByteArray(), ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        bout.reset();
        yuv.compressToJpeg(rect, 90, bout);
        final byte[] jpeg = bout.toByteArray();
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
    }
}
