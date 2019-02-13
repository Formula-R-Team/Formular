package io.github.formular_team.formular;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.formular_team.formular.tracer.BilinearMapper;
import io.github.formular_team.formular.tracer.OrientFunction;
import io.github.formular_team.formular.tracer.PathBuilder;
import io.github.formular_team.formular.tracer.PathReader;
import io.github.formular_team.formular.tracer.SimpleStepFunction;
import io.github.formular_team.formular.tracer.TransformMapper;
import io.github.formular_team.formular.tracer.Vec2;

// https://inducesmile.com/android/android-camera2-api-example-tutorial/
public final class Formular extends CordovaPlugin {
    @Override
    public void pluginInitialize() {
        super.pluginInitialize();
        final Activity activity = this.cordova.getActivity();
        textureView = activity.findViewById(R.id.camera_view);
    }

    @Override
    public void onDestroy() {
        this.closeCamera();
        super.onDestroy();
    }

    @Override
    public void onPause(final boolean multitasking) {
        super.onPause(multitasking);
        this.onPause();
    }

    @Override
    public void onResume(final boolean multitasking) {
        super.onResume(multitasking);
        this.onResume();
    }

    private CallbackContext context;

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("camera/start_capture".equals(action)) {
            if (this.context == null) {
                this.context = callbackContext;
            } else {
//				callbackContext.error("Capture already active");
//				return false;
            }
            return true;
        } else if ("camera/end_capture".equals(action)) {
            // TODO: thread safeness
            this.context = null;
            return true;
        } else if ("camera/capture_still".equals(action)) {
            //final PluginResult result = new PluginResult(PluginResult.Status.OK);
            //result.setKeepCallback(true);
            //callbackContext.sendPluginResult(result);
            //captureStill();
            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraDevice cameraDevice;

    private CameraCaptureSession cameraCaptureSessions;

    private CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimension;

    private ImageReader imageReader;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private Handler mBackgroundHandler;

    private HandlerThread mBackgroundThread;

    private final SurfaceListener textureListener = new SurfaceListener();

    private class SurfaceListener implements TextureView.SurfaceTextureListener {
        private int w, h;

        private Bitmap map;

        private Bitmap buf;

        private Canvas canvas;

        private final float scale = 0.12F;

        private long time;

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            this.resize(width, height);
            Formular.this.openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
            this.resize(width, height);
        }

        private void resize(final int width, final int height) {
            this.w = width;
            this.h = height;
            this.map = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
            this.buf = Bitmap.createBitmap((int) (this.w * this.scale), (int) (this.h * this.scale), Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(this.buf);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            if (Formular.this.context != null) {
                final long now = System.currentTimeMillis();
                if (now - this.time > 1000.0 / 30.0) {
                    this.time = now;
                    Formular.this.textureView.getBitmap(this.map);
                    this.canvas.save();
                    this.canvas.scale(this.scale, this.scale);
                    this.canvas.drawBitmap(this.map, 0.0F, 0.0F, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                    this.canvas.restore();
                    final float dx = this.buf.getWidth() / 2.0F;
                    final float dy = this.buf.getHeight() / 2.0F;
                    final int r = 8;
                    double greatest = Double.NEGATIVE_INFINITY;
                    final TransformMapper mapper = new TransformMapper(new BilinearMapper((x, y) -> {
                        if (x >= 0 && x < this.buf.getWidth() && y >= 0 && y < this.buf.getHeight()) {
                            return 1.0D - Formular.this.brightness(this.buf.getPixel(x, y));
                        }
                        return 0.0D;
                    }), dx, dy, 0.0D);
                    int km = 0, jm = 0;
                    for (int k = -r; k <= r; k++) {
                        for (int j = -r; j <= r; j++) {
                            final double v = mapper.get(k, j);
                            if (v > greatest) {
                                greatest = v;
                                km = k;
                                jm = j;
                            }
                        }
                    }
                    mapper.setX(dx + km);
                    mapper.setY(dy + jm);
                    final JSONArray arr = new JSONArray();
                    final boolean[] closed = new boolean[1];
                    new PathReader(
                        new SimpleStepFunction(5, Math.PI / 2.0D),
                        new OrientFunction(2)
                    ).read(mapper, new PathBuilder() {
                        boolean bad = false;

                        @Override
                        public void moveTo(final double x, final double y) {
                            this.put(x, y);
                        }

                        @Override
                        public void lineTo(final double x, final double y) {
                            this.put(x, y);
                        }

                        private void put(final double x, final double y) {
                            if (this.bad) {
                                return;
                            }
                            final Vec2 v = mapper.transformPoint(new Vec2(x, y));
                            v.mul(new Vec2(1.0D / SurfaceListener.this.buf.getWidth(), 1.0D / SurfaceListener.this.buf.getHeight()));
                            final JSONArray a = Formular.this.pos(v.getX(), v.getY());
                            if (a.length() == 2) {
                                arr.put(a);
                            } else {
                                this.bad = true;
                            }
                        }

                        @Override
                        public void closePath() {
                            closed[0] = true;
                        }
                    });
                    final JSONObject obj = new JSONObject();
                    try {
                        obj.put("segments", arr);
                        obj.put("closed", closed[0]);
						/*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						this.buf.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
						byte[] byteArray = byteArrayOutputStream .toByteArray();
						obj.put("image", Base64.encodeToString(byteArray, Base64.NO_WRAP));*/
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                    final PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    Formular.this.context.sendPluginResult(result);
                }
            }
        }
    }

    private JSONArray pos(final double x, final double y) {
        final JSONArray pos = new JSONArray();
        try {
            pos.put(x);
            pos.put(y);
        } catch (final JSONException ignored) {
        }
        return pos;
    }

    private double brightness(final int rgb) {
        return (double) Math.max(rgb >> 16 & 0xFF, Math.max(rgb >> 8 & 0xFF, rgb & 0xFF)) / 0xFF;
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(final CameraDevice camera) {
            Formular.this.cameraDevice = camera;
            Formular.this.createCameraPreview();
        }

        @Override
        public void onDisconnected(final CameraDevice camera) {
            Formular.this.cameraDevice.close();
        }

        @Override
        public void onError(final CameraDevice camera, final int error) {
            Formular.this.cameraDevice.close();
            Formular.this.cameraDevice = null;
        }
    };

    private final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(final CameraCaptureSession session, final CaptureRequest request, final TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Formular.this.createCameraPreview();
        }
    };

    private void startBackgroundThread() {
        this.mBackgroundThread = new HandlerThread("Camera Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new Handler(this.mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (this.mBackgroundThread != null) {
            this.mBackgroundThread.quitSafely();
            try {
                this.mBackgroundThread.join();
                this.mBackgroundThread = null;
                this.mBackgroundHandler = null;
            } catch (final InterruptedException ignored) {
            }
        }
    }

    private void captureStill() {
        if (this.cameraDevice == null) {
            return;
        }
        final CameraManager manager = (CameraManager) this.cordova.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            final CameraCharacteristics characteristics = Objects.requireNonNull(manager).getCameraCharacteristics(this.cameraDevice.getId());
            final Size[] jpegSizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(ImageFormat.JPEG);
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(this.textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            final int rotation = this.cordova.getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final ImageReader.OnImageAvailableListener readerListener = reader1 -> {
                try (final Image image = reader1.acquireLatestImage()) {
                    image.getPlanes()[0].getBuffer();
                }
            };
            reader.setOnImageAvailableListener(readerListener, this.mBackgroundHandler);
            this.cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(final CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), Formular.this.captureCallbackListener, Formular.this.mBackgroundHandler);
                    } catch (final CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(final CameraCaptureSession session) {}
            }, this.mBackgroundHandler);
        } catch (final CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            final SurfaceTexture texture = this.textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(this.imageDimension.getWidth(), this.imageDimension.getHeight());
            final Surface surface = new Surface(texture);
            this.captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            this.captureRequestBuilder.addTarget(surface);
            this.cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                    if (Formular.this.cameraDevice != null) {
                        Formular.this.cameraCaptureSessions = cameraCaptureSession;
                        Formular.this.updatePreview();
                    }
                }

                @Override
                public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {}
            }, null);
        } catch (final CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        final CameraManager manager = (CameraManager) this.cordova.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            final String cameraId = Objects.requireNonNull(manager).getCameraIdList()[0];
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            final StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            this.imageDimension = Objects.requireNonNull(map).getOutputSizes(SurfaceTexture.class)[0];
            if (this.cordova.getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                this.cordova.getActivity().requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION);
            } else {
                manager.openCamera(cameraId, this.stateCallback, null);
            }
        } catch (final CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (this.cameraDevice != null) {
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            try {
                this.cameraCaptureSessions.setRepeatingRequest(this.captureRequestBuilder.build(), null, this.mBackgroundHandler);
            } catch (final CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCamera() {
        if (null != this.cameraDevice) {
            this.cameraDevice.close();
            this.cameraDevice = null;
        }
        if (null != this.imageReader) {
            this.imageReader.close();
            this.imageReader = null;
        }
    }

    private void onResume() {
        this.startBackgroundThread();
        if (this.textureView.isAvailable()) {
            this.openCamera();
        } else {
            this.textureView.setSurfaceTextureListener(this.textureListener);
        }
    }


    private void onPause() {
        this.closeCamera();
        this.stopBackgroundThread();
    }
}
