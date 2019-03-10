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

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import io.github.formular_team.formular.geometry.ExtrudeGeometry;
import io.github.formular_team.formular.math.Bezier;
import io.github.formular_team.formular.math.CubicBezierCurve;
import io.github.formular_team.formular.math.CubicBezierCurve3;
import io.github.formular_team.formular.math.Curve;
import io.github.formular_team.formular.math.CurvePath;
import io.github.formular_team.formular.math.Float32Array;
import io.github.formular_team.formular.math.LineCurve3;
import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.PathVisitor;
import io.github.formular_team.formular.math.Ray;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.TransformingPathVisitor;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;
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

    private ImageView overlayView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.overlayView = this.findViewById(R.id.overlay);
        final ArFragment fragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        final ArSceneView view = fragment.getArSceneView();
        view.getScene().setOnTouchListener((hit, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }
            final Frame frame = view.getArFrame();
            if (frame == null) {
                return false;
            }
            for (final HitResult result : frame.hitTest(event))
                if (result.getTrackable() instanceof Plane) {
                    final Plane plane = (Plane) result.getTrackable();
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
                    final float[] buf = new float[16];
                    camera.getProjectionMatrix(buf, 0, 0.1F, 6.0F);
                    for (int n = 0; n < 16; n++) {
                        projMat.getArray().set(n, buf[n]);
                    }
                    final Matrix4 viewMat = new Matrix4();
                    camera.getViewMatrix(buf, 0);
                    for (int n = 0; n < 16; n++) {
                        viewMat.getArray().set(n, buf[n]);
                    }
                    final Float32Array in2 = Float32Array.create(2);
                    in2.set(0, event.getX());
                    in2.set(1, event.getY());
                    final Float32Array out2 = Float32Array.create(2);
                    frame.transformCoordinates2d(
                        Coordinates2d.VIEW, in2.getTypedBuffer(),
                        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, out2.getTypedBuffer()
                    );
                    final Ray pickRay = Projection.unproject(
                        new Vector2().fromArray(out2),
                        projMat,
                        viewMat
                    );
                    final float captureRange = 0.15F;
                    final float gameRoadWidth = 4.0F;
                    final float gameToSceneScale = 0.06F / gameRoadWidth; // physical 6cm = virtual roadWidth meters
                    pickRay.intersectPlane(fplane, new io.github.formular_team.formular.math.Vector3())
                        .ifPresent(v -> {
                            final Pose planeAtPick = Pose.makeTranslation(v.getX(), v.getY(), v.getZ()).compose(planePose.extractRotation());
                            final Matrix4 planeAtPickModelMatrix = new Matrix4();
                            planeAtPick.toMatrix(buf, 0);
                            for (int n = 0; n < 16; n++) {
                                planeAtPickModelMatrix.getArray().set(n, buf[n]);
                            }
                            final Function<Vector3, Vector2> planeToImage = in -> {
                                final io.github.formular_team.formular.math.Vector3 ndc = in.apply(planeAtPickModelMatrix)
                                    .apply(viewMat)
                                    .applyProjection(projMat);
                                in2.set(0, ndc.getX());
                                in2.set(1, ndc.getY());
                                frame.transformCoordinates2d(
                                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, in2.getTypedBuffer(),
                                    Coordinates2d.IMAGE_PIXELS, out2.getTypedBuffer()
                                );
                                return new Vector2().fromArray(out2);
                            };
                            final Vector2 b00 = planeToImage.apply(new Vector3(-captureRange, 0.0F, captureRange));
                            final Vector2 b01 = planeToImage.apply(new Vector3(-captureRange, 0.0F, -captureRange));
                            final Vector2 b10 = planeToImage.apply(new Vector3(captureRange, 0.0F, captureRange));
                            final Vector2 b11 = planeToImage.apply(new Vector3(captureRange, 0.0F, -captureRange));
                            final Vector2 min, max;
                            final Bitmap image;
                            final int captureSize = 128;
                            try (final Image cameraImage = frame.acquireCameraImage()) {
                                min = b00.clone().min(b01).min(b10).min(b11).floor().max(new Vector2(0, 0));
                                max = b00.clone().max(b01).max(b10).max(b11).ceil().min(new Vector2(cameraImage.getWidth() - 1, cameraImage.getHeight() - 1));
                                image = Images.yuvToBitmap(cameraImage, new Rect((int) min.getX(), (int) min.getY(), (int) max.getX(), (int) max.getY()));
                            } catch (final NotYetAvailableException e) {
                                throw new RuntimeException(e);
                            }
                            if (image != null) {
                                final Bitmap capture = Bitmap.createBitmap(captureSize, captureSize, Bitmap.Config.ARGB_8888);
                                final Vector3 outputPos = new Vector3();
                                for (int y = 0; y < capture.getHeight(); y++) {
                                    for (int x = 0; x < capture.getWidth(); x++) {
                                        outputPos.set(
                                            captureRange * (2.0F * x / captureSize - 1.0F),
                                            0.0F,
                                            -captureRange * (2.0F * y / captureSize - 1.0F)
                                        );
                                        final Vector2 p = planeToImage.apply(outputPos).sub(min);
                                        if (p.getX() >= 0.0F && p.getY() >= 0.0F && p.getX() < image.getWidth() && p.getY() < image.getHeight()) {
                                            // TODO: bilinear interpolation?
                                            capture.setPixel(x, y, image.getPixel((int) p.getX(), (int) p.getY()));
                                        }
                                    }
                                }
                                final Path captureSegments = this.findPath(capture);
//                                final StringBuilder bob = new StringBuilder("final float[][] points = {");
//                                for (final Vector2 ve : captureSegments.getPoints(true)) {
//                                    bob.append(" { ").append(ve.getX()).append("F, ").append(ve.getY()).append("F },");
//                                }
//                                Log.i("waldo", bob.append(" };").toString());
//                                final float[][] points = { { 49.123573F, 69.88999F }, { 49.19941F, 76.88958F }, { 46.24782F, 83.23688F }, { 40.406498F, 87.0942F }, { 34.338467F, 90.584045F }, { 29.385218F, 95.53029F }, { 29.747696F, 102.52089F }, { 32.56315F, 108.92973F }, { 38.024254F, 113.30881F }, { 44.830616F, 114.94388F }, { 51.463776F, 117.180214F }, { 58.452126F, 117.58394F }, { 65.36386F, 116.47583F }, { 72.225494F, 115.0909F }, { 78.21353F, 111.465515F }, { 82.16871F, 105.69F }, { 84.65629F, 99.14692F }, { 86.03334F, 92.28371F }, { 86.420395F, 85.29441F }, { 85.90199F, 78.31364F }, { 85.158066F, 71.35328F }, { 84.397095F, 64.39476F }, { 83.15025F, 57.506702F }, { 81.94709F, 50.610878F }, { 80.7159F, 43.72F }, { 78.47067F, 37.089848F }, { 73.316444F, 32.353397F }, { 67.345314F, 28.700237F }, { 60.623825F, 26.745358F }, { 53.691288F, 25.775856F }, { 46.691444F, 25.823013F }, { 40.02389F, 27.954613F }, { 33.51162F, 30.521774F }, { 27.919603F, 34.7324F }, { 23.383804F, 40.064053F }, { 21.79696F, 46.88182F }, { 23.84183F, 53.57648F }, { 29.241438F, 58.031166F }, { 35.712284F, 60.701023F }, { 42.07285F, 63.6239F }, { 49.123573F, 69.88999F } };
//                                final Path captureSegments = new Path();
//                                captureSegments.moveTo(points[0][0], points[0][1]);
//                                for (int i = 1; i < points.length; i++) {
//                                    captureSegments.lineTo(points[i][0], points[i][1]);
//                                }
                                final Path capturePath = Bezier.fitBezierCurve(captureSegments, 8.0F);
                                this.updateOverlayPath(capture, capturePath);
                                final Path trackPath = new Path();
                                final float gameRange = captureRange / gameToSceneScale;
                                capturePath.visit(new TransformingPathVisitor(trackPath, new Matrix3()
                                    .scale(2.0F / captureSize)
                                    .translate(-1.0F, -1.0F)
                                    .scale(gameRange, -gameRange)
                                ));
                                this.tracks.forEach(view.getScene()::removeChild);
                                this.tracks.clear();
                                final Anchor planeAnchor = plane.createAnchor(planeAtPick);
                                final AnchorNode planeAnchorNode = new AnchorNode(planeAnchor);
                                this.tracks.add(planeAnchorNode);
                                planeAnchorNode.setParent(view.getScene());
                                final int courseDiffuseSize = 512;
                                final Bitmap courseDiffuse = Bitmap.createBitmap(courseDiffuseSize, courseDiffuseSize, Bitmap.Config.ARGB_8888);
                                {
                                    final android.graphics.Path coursePath = new android.graphics.Path();
                                    capturePath.visit(new TransformingPathVisitor(new GraphicsPathVisitor(coursePath), new Matrix3()
                                        .scale(courseDiffuseSize / (float) captureSize)
                                    ));
                                    coursePath.close();
                                    final float gameToCoursePixel = courseDiffuseSize / (2.0F * gameRange);
                                    final Canvas canvas = new Canvas(courseDiffuse);
                                    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                    paint.setStyle(Paint.Style.FILL);
                                    final int road = 0xFF565E66;
                                    paint.setColor(road);
                                    canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setStrokeWidth(gameRoadWidth * 0.85F * gameToCoursePixel);
                                    paint.setColor(0xFFFFFFFF);
                                    canvas.drawPath(coursePath, paint);
                                    paint.setStrokeWidth(gameRoadWidth * 0.78F * gameToCoursePixel);
                                    paint.setColor(road);
                                    canvas.drawPath(coursePath, paint);
                                }
                                Texture.builder().setSource(courseDiffuse).build().thenAccept(diffuse ->
                                    MaterialFactory.makeOpaqueWithTexture(MainActivity.this, diffuse).thenAccept(material -> {
                                        final float roadHeight = 0.225F;
                                        final float roadHalfWidth = gameRoadWidth * 0.5F;
                                        final Shape shape = new Shape();
                                        shape.moveTo(0.0F, -roadHalfWidth);
                                        shape.lineTo(-roadHeight, -roadHalfWidth);
                                        shape.lineTo(-roadHeight, roadHalfWidth);
                                        shape.lineTo(0.0F, roadHalfWidth);
                                        shape.lineTo(0.0F, -roadHalfWidth);
                                        final CurvePath trackPath3d = new CurvePath();
                                        trackPath.visit(new PathVisitor() {
                                            private Vector3 last = new Vector3();

                                            @Override
                                            public void moveTo(final float x, final float y) {
                                                this.last = this.map(x, y);
                                            }

                                            @Override
                                            public void lineTo(final float x, final float y) {
                                                trackPath3d.add(new LineCurve3(this.last, this.last = this.map(x, y)));
                                            }

                                            @Override
                                            public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
                                                trackPath3d.add(new CubicBezierCurve3(this.last, this.map(aCP1x, aCP1y), this.map(aCP2x, aCP2y), this.last = this.map(aX, aY)));
                                            }

                                            @Override
                                            public void closePath() {}

                                            private Vector3 map(final float x, final float y) {
                                                return new Vector3(x, 0.0F, y);
                                            }
                                        });
//                                        final StringBuilder bob = new StringBuilder();
//                                        final Consumer<Vector3> addV = vec3 -> bob.append("new THREE.Vector3(").append(vec3.getX()).append(", ").append(vec3.getY()).append(", ").append(vec3.getZ()).append(")");
//                                        for (final Curve c : trackPath3d.getCurves()) {
//                                            final CubicBezierCurve3 cbc = (CubicBezierCurve3) c;
//                                            bob.append("bezierPath.add(new THREE.CubicBezierCurve3(");
//                                            addV.accept(cbc.v0);
//                                            bob.append(", ");
//                                            addV.accept(cbc.v1);
//                                            bob.append(", ");
//                                            addV.accept(cbc.v2);
//                                            bob.append(", ");
//                                            addV.accept(cbc.v3);
//                                            bob.append("));");
//                                        }
//                                        Log.i("waldo", bob.toString());
                                        final ModelRenderable pathRenderable = Geometries.toRenderable(shape.extrude(new ExtrudeGeometry.ExtrudeGeometryParameters() {{
                                            this.steps = 8 * trackPath3d.getCurves().size();
                                            this.extrudePath = trackPath3d;
                                            this.uvGenerator = new ExtrudeGeometry.WorldUVGenerator(new Matrix4()
                                                .multiply(new Matrix4().makeTranslation(0.5F, 0.0F, 0.5F))
                                                .multiply(new Matrix4().makeScale(0.5F / gameRange, 0.0F, 0.5F / gameRange))
                                            );
                                        }}), material);
                                        final Node pathNode = new Node();
                                        pathNode.setLocalScale(com.google.ar.sceneform.math.Vector3.one().scaled(gameToSceneScale));
                                        pathNode.setParent(planeAnchorNode);
                                        pathNode.setRenderable(pathRenderable);
                                }));
                            }
                        });
                    return true;
                }
            return false;
        });
        view.getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
            boolean needUpdate = false;

            @Override
            public void onUpdate(final FrameTime frameTime) {
                final Session session;
                if (this.needUpdate && (session = fragment.getArSceneView().getSession()) != null) {
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
    }

    private List<Node> tracks = new ArrayList<>();

    private Path findPath(final Bitmap capture) {
        final TransformMapper mapper = new TransformMapper(new BilinearMapper(new ImageLineMap(new BitmapImageMap(capture))), 0.0F, 0.0F, 0.0F);
        // TODO: use vector2
        float startX = 0.0F, startY = 0.0F;
        float startLine = Float.NEGATIVE_INFINITY;
        final float radius = 0.125F * Math.min(capture.getWidth(), capture.getHeight());
        final float originX = 0.5F * capture.getWidth(), originY = 0.5F * capture.getHeight();
        final int circum = (int) (2.0F * Mth.PI * radius);
        for (int n = 0; n < circum; n++) {
            final float theta = 2.0F * Mth.PI * n / circum;
            final float x = originX + radius * Mth.cos(theta);
            final float y = originY + radius * Mth.sin(theta);
            final float line = mapper.get(x, y);
            if (line > startLine) {
                startX = x;
                startY = y;
                startLine = line;
            }
        }
        mapper.setTranslation(startX, startY);
        final Path capturePath = new Path();
        new PathReader(
            new SimpleStepFunction(7, (0.5F * Mth.PI)),
            new OrientFunction(3)
        ).read(mapper, new TransformingPathVisitor(capturePath, mapper.getMatrix()));
        return capturePath;
    }

    private void updateOverlayPath(final Bitmap capture, final Path pathInCapture) {
        if (this.overlayView != null) {
            final android.graphics.Path graphicPath = new android.graphics.Path();
            pathInCapture.visit(new GraphicsPathVisitor(graphicPath));
            graphicPath.close();
            final Canvas canvas = new Canvas(capture);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3.0F);
            paint.setColor(0x70FF1A52);
            canvas.drawPath(graphicPath, paint);
            paint.setColor(0xE0FF1A52);
            // TODO: CurveVisitor
            for (final Curve c : pathInCapture.getCurves()) {
                final CubicBezierCurve cc = (CubicBezierCurve) c;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xF0FF1A52);
                canvas.drawCircle(cc.v0.getX(), cc.v0.getY(), 2.0F, paint);
                paint.setColor(0x90FF1A52);
                canvas.drawCircle(cc.v1.getX(), cc.v1.getY(), 1.5F, paint);
                canvas.drawCircle(cc.v2.getX(), cc.v2.getY(), 1.5F, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.0F);
                canvas.drawLine(cc.v0.getX(), cc.v0.getY(), cc.v1.getX(), cc.v1.getY(), paint);
                canvas.drawLine(cc.v3.getX(), cc.v3.getY(), cc.v2.getX(), cc.v2.getY(), paint);
            }
            this.overlayView.setImageBitmap(capture);
        }
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
