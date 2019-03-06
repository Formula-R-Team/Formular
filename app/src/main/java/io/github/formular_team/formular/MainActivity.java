package io.github.formular_team.formular;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Comparator;
import java.util.function.Function;

import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.PathVisitor;
import io.github.formular_team.formular.math.Ray;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.server.Game;
import io.github.formular_team.formular.server.ServerController;
import io.github.formular_team.formular.server.SimpleServer;
import io.github.formular_team.formular.trace.BilinearMapper;
import io.github.formular_team.formular.trace.ImageLineMap;
import io.github.formular_team.formular.trace.OrientFunction;
import io.github.formular_team.formular.trace.PathReader;
import io.github.formular_team.formular.trace.SimpleStepFunction;
import io.github.formular_team.formular.trace.TransformMapper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ServerController controller;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        final ArFragment fragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        final ArSceneView view = fragment.getArSceneView();
        final ImageView overlayView = this.findViewById(R.id.overlay);
        view.getScene().setOnTouchListener((hit, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }
            final Frame frame = view.getArFrame();
            if (frame == null) {
                return false;
            }
            // frame.hitTest(view.getWidth() / 2.0F, view.getHeight() / 2.0F)
            for (final HitResult result : frame.hitTest(event))
                if (result.getTrackable() instanceof Plane) {
                    final Plane plane = (Plane) result.getTrackable();
                    final Pose pose = result.getHitPose();
                    /*final AnchorNode node = new AnchorNode(plane.createAnchor(pose));
                    MaterialFactory.makeOpaqueWithColor(MainActivity.this, new Color(0x0F42DA))
                        .thenAccept(material -> node.setRenderable(
                            ShapeFactory.makeCube(new Vector3(0.15F, 0.15F, 0.15F), new Vector3(0.0F, 0.15F / 2.0F, 0.0F), material)
                        ));
                    node.setParent(view.getScene());*/

                    final Pose planePose = plane.getCenterPose();
                    final float[] planeNormal = planePose.getYAxis();
                    final float[] planeTranslation = planePose.getTranslation();
                    final io.github.formular_team.formular.math.Plane fplane = new io.github.formular_team.formular.math.Plane();
                    fplane.setFromNormalAndCoplanarPoint(
                        new io.github.formular_team.formular.math.Vector3(planeNormal[0], planeNormal[1], planeNormal[2]),
                        new io.github.formular_team.formular.math.Vector3(planeTranslation[0], planeTranslation[1], planeTranslation[2])
                    );
                    final Camera camera = frame.getCamera();
                    final Matrix4 projMat = new Matrix4();
                    camera.getProjectionMatrix(projMat.elements(), 0, 0.01F, 10.0F);
                    final Matrix4 viewMat = new Matrix4();
                    camera.getViewMatrix(viewMat.elements(), 0);

                    final float[] in2 = new float[2];
                    final float[] out2 = new float[2];
                    in2[0] = event.getX();
                    in2[1] = event.getY();
                    frame.transformCoordinates2d(
                        Coordinates2d.VIEW, in2,
                        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, out2
                    );
                    final Ray pickRay = Projection.unproject(
                        new Vector2().fromArray(out2),
                        projMat,
                        viewMat
                    );
                    final int captureSize = 128;
                    final float scale = 0.075F;
                    pickRay.intersectPlane(fplane, new io.github.formular_team.formular.math.Vector3())
                        .ifPresent(v -> {
                            final Pose planeAtPick = Pose.makeTranslation(v.toArray(new float[3])).compose(planePose.extractRotation());
                            final Matrix4 planeModelMatrix = new Matrix4();
                            planeAtPick.toMatrix(planeModelMatrix.elements(), 0);
                            final Function<Vector2, Vector2> project = in -> {
                                final io.github.formular_team.formular.math.Vector3 ndc = new io.github.formular_team.formular.math.Vector3(
                                    scale * (2.0F * in.x() / captureSize - 1.0F),
                                    0,
                                    -scale * (2.0F * in.y() / captureSize - 1.0F)
                                ).applyMatrix4(planeModelMatrix).applyMatrix4(viewMat).applyMatrix4(projMat);
                                in2[0] = ndc.x();
                                in2[1] = ndc.y();
                                frame.transformCoordinates2d(
                                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, in2,
                                    Coordinates2d.IMAGE_PIXELS, out2
                                );
                                return new Vector2().fromArray(out2);
                            };
                            final Vector2 b00 = project.apply(new Vector2(0, 0));
                            final Vector2 b01 = project.apply(new Vector2(0, captureSize - 1));
                            final Vector2 b10 = project.apply(new Vector2(captureSize - 1, 0));
                            final Vector2 b11 = project.apply(new Vector2(captureSize - 1, captureSize - 1));
                            final Vector2 min, max;
                            final Bitmap image;
                            try (final Image cameraImage = frame.acquireCameraImage()) {
                                min = b00.copy().min(b01).min(b10).min(b11).floor().max(new Vector2(0, 0));
                                max = b00.copy().max(b01).max(b10).max(b11).ceil().min(new Vector2(cameraImage.getWidth() - 1, cameraImage.getHeight() - 1));
                                image = Images.yuvToBitmap(cameraImage, new Rect((int) min.x(), (int) min.y(), (int) max.x(), (int) max.y()));
                            } catch (final NotYetAvailableException e) {
                                throw new RuntimeException(e);
                            }
                            final Bitmap map = Bitmap.createBitmap(captureSize, captureSize, Bitmap.Config.ARGB_8888);
                            final Vector2 outputPos = new Vector2();
                            for (int y = 0; y < captureSize; y++) {
                                for (int x = 0; x < captureSize; x++) {
                                    final Vector2 p = project.apply(outputPos.set(x, y)).sub(min);
                                    if (p.x() >= 0.0F && p.y() >= 0.0F && p.x() < image.getWidth() && p.y() < image.getHeight()) {
                                        // TODO: bilinear interpolation?
                                        map.setPixel(x, y, image.getPixel((int) p.x(), (int) p.y()));
                                    }
                                }
                            }
                            final TransformMapper mapper = new TransformMapper(new BilinearMapper(new ImageLineMap(new BitmapImageMap(map))), 0.0F, 0.0F, 0.0F);

                            float startX = 0, startY = 0, startLine = Float.NEGATIVE_INFINITY;
                            // spiral search
                            /*final int searchSize = captureSize / 4, searchOffset = captureSize / 2 - searchSize / 2, searchRadius = (searchSize + 1) / 2;
                            for (int i = 0, x = 0, y = 0; i < searchSize * searchSize; i++) {
                                final float distance = Math.min((float) (x * x + y * y) / (searchRadius * searchRadius), 1.0F);
                                final float weight = (float) Math.sqrt(1.0F - distance * distance);
                                final float line = mapper.get(searchOffset + x, searchOffset + y) * weight;
                                if (line > startLine) {
                                    startX = searchOffset + x;
                                    startY = searchOffset + y;
                                    startLine = line;
                                }
                                if (Math.abs(x) <= Math.abs(y) && (x != y || x >= 0)) {
                                    x += y >= 0 ? 1 : -1;
                                } else {
                                    y += x >= 0 ? -1 : 1;
                                }
                            }*/
                            // circle perimeter search
                            final float radius = 0.125F * captureSize;
                            final float origin = 0.5F * captureSize;
                            final int circum = (int) (2.0F * Math.PI * radius);
                            for (int n = 0; n < circum; n++) {
                                final float theta = (float) (2.0F * Math.PI * n / circum);
                                final float x = (float) (origin + radius * Math.cos(theta));
                                final float y = (float) (origin + radius * Math.sin(theta));
                                final float line = mapper.get(x, y);
                                if (line > startLine) {
                                    startX = x;
                                    startY = y;
                                    startLine = line;
                                }
                            }
                            mapper.setX(startX);
                            mapper.setY(startY);
                            final Path.Builder path = Path.builder();
                            new PathReader(
                                new SimpleStepFunction(5, (float) Math.PI / 2.0F),
                                new OrientFunction(2)
                            ).read(mapper, path);
                            final Path p = path.build();
                            final android.graphics.Path gpath = new android.graphics.Path();
                            p.visit(new PathVisitor() {
                                @Override
                                public PathVisitor moveTo(final Vector2 point) {
                                    final Vector2 p = this.prepare(point);
                                    gpath.moveTo(p.x(), p.y());
                                    return this;
                                }

                                @Override
                                public PathVisitor lineTo(final Vector2 point) {
                                    final Vector2 p = this.prepare(point);
                                    gpath.lineTo(p.x(), p.y());
                                    return this;
                                }

                                private Vector2 prepare(final Vector2 point) {
                                    return mapper.transformPoint(point);
                                }

                                @Override
                                public PathVisitor bezierCurveTo(final Vector2 controlA, final Vector2 controlB, final Vector2 point) {
                                    return this;
                                }

                                @Override
                                public PathVisitor closePath() {
                                    return this;
                                }
                            });
                            final Canvas canvas = new Canvas(map);
                            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(4.0F);
                            paint.setColor(0x75FF1A52);
                            canvas.drawPath(gpath, paint);
                            overlayView.setImageBitmap(map);
                        });
                    return true;
                }
            return false;
        });
        view.getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
            boolean needUpdate = false;

            @Override
            public void onUpdate(final FrameTime frameTime) {
                if (this.needUpdate) {
                    final Session session = fragment.getArSceneView().getSession();
                    session.pause();
                    session.getSupportedCameraConfigs().stream()
                        .max(Comparator.comparingDouble(config -> config.getImageSize().getHeight()))
                        .ifPresent(session::setCameraConfig);
                    try {
                        session.resume();
                    } catch (final CameraNotAvailableException e) {
                        throw new AssertionError(e);
                    }
                    this.needUpdate = false;
                }
            }
        });
        /*ModelRenderable.builder()
            .setSource(this, Uri.parse("teapot.sfb"))
            .build()
            .thenAccept(renderable -> {
                Node node = new Node();
                node.setParent(fragment.getArSceneView().getScene());
                node.setRenderable(renderable);
            })
            .exceptionally(throwable -> {
                Log.e("formular", "Unable to load Renderable.", throwable);
                return null;
            });*/

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Game game = new Game();
        this.controller = ServerController.create(SimpleServer.create(game, 20));
        this.controller.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.controller.stop();
    }
}
