package io.github.formular_team.formular;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import io.github.formular_team.formular.car.KartDefinition;
import io.github.formular_team.formular.car.KartModel;
import io.github.formular_team.formular.geometry.ExtrudeGeometry;
import io.github.formular_team.formular.math.Bezier;
import io.github.formular_team.formular.math.CubicBezierCurve3;
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
import io.github.formular_team.formular.trace.BilinearMapper;
import io.github.formular_team.formular.trace.ImageLineMap;
import io.github.formular_team.formular.trace.OrientFunction;
import io.github.formular_team.formular.trace.PathReader;
import io.github.formular_team.formular.trace.SimpleStepFunction;
import io.github.formular_team.formular.trace.TransformMapper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ModelLoader modelLoader;

    private static final int KART_BODY = 0, KART_WHEEL = 1;

    private ModelRenderable kartBody, kartWheel;

    private Node courseNode, kartNode;

    private KartModel kart;

//    private ServerController controller;

    private ImageView overlayView;

    private ArFragment arFragment;

    public void setRenderable(final int id, final ModelRenderable modelRenderable) {
        if (id == KART_BODY) {
            this.kartBody = modelRenderable;
        } else if (id == KART_WHEEL) {
            this.kartWheel = modelRenderable;
        }
    }

    public void onException(final int id, final Throwable throwable) {
        final Toast toast = Toast.makeText(this, "Unable to load renderable: " + id, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        Log.e(TAG, "Unable to load renderable", throwable);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.overlayView = this.findViewById(R.id.overlay);
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        this.modelLoader = new ModelLoader(this);
        this.modelLoader.loadModel(KART_BODY, R.raw.kart);
        this.modelLoader.loadModel(KART_WHEEL, R.raw.wheel);
        this.arFragment.setOnTapArPlaneListener(this::onPlaneTap);
        this.createJoystick();
        this.findViewById(R.id.reset).setOnClickListener(v -> {
            if (MainActivity.this.courseNode != null) {
                MainActivity.this.arFragment.getArSceneView().getScene().removeChild(MainActivity.this.courseNode);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createJoystick() {
        final View joystick = this.findViewById(R.id.joystick);
        joystick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // TODO: flip x/y if landscape orientation
                final float x = Mth.clamp(event.getX() / v.getWidth() * 2.0F - 1.0F, -1.0F, 1.0F);
                final float y = Mth.clamp(event.getY() / v.getHeight() * 2.0F - 1.0F, -1.0F, 1.0F);
                if (this.kart != null) {
                    this.kart.steerangle = -Mth.PI / 4.0F * x;
                    this.kart.throttle = Math.max(-y, 0.0F) * 40;
                    this.kart.brake = Math.max(y, 0.0F) * 100;
                }
            case MotionEvent.ACTION_UP:
                return true;
            }
            return false;
        });
    }

    private KartDefinition createKartDefinition() {
        final KartDefinition definition = new KartDefinition();
        definition.wheelbase = 1.982F;
        final float t = 0.477F;
        definition.b = (1.0F - t) * definition.wheelbase;
        definition.c = t * definition.wheelbase;
        definition.h = 0.7F;
        definition.mass = 1100.0F;
        definition.inertia = 1100.0F;
        definition.width = 1.1176F;
        definition.length = 2.794F;
        definition.wheelradius = 0.248F;
        definition.tireGrip = 2.2F;
        definition.caF = -6.0F;
        definition.caR = -6.2F;
        return definition;
    }

    private void onPlaneTap(final HitResult hitResult, final Plane scenePlane, final MotionEvent event) {
        if (this.kartBody == null || this.kartWheel == null) {
            return;
        }
        final ArSceneView view = this.arFragment.getArSceneView();
        final Frame frame = view.getArFrame();
        if (frame == null) {
            throw new AssertionError();
        }
        final Pose planePose = scenePlane.getCenterPose();
        final float[] planeNormal = planePose.getYAxis();
        final float[] planeTranslation = planePose.getTranslation();
        final io.github.formular_team.formular.math.Plane plane = new io.github.formular_team.formular.math.Plane();
        plane.setFromNormalAndCoplanarPoint(
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
        final float courseRoadWidth = 6.0F;
        final float courseRoadHalfWidth = 0.5F * courseRoadWidth;
        final float courseToSceneScale = 0.05F / courseRoadWidth;
        final io.github.formular_team.formular.math.Vector3 v = new io.github.formular_team.formular.math.Vector3();
        if (pickRay.intersectPlane(plane, v)) {
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
            if (image == null) {
                return;
            }
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
            final Path captureTrackPath = Bezier.fitBezierCurve(captureSegments, 8.0F);
//                this.updateOverlayPath(capture, captureTrackPath);
            final Path courseTrackPath = new Path();
            final float courseCaptureSize = captureRange / courseToSceneScale;
            captureTrackPath.visit(new TransformingPathVisitor(courseTrackPath, new Matrix3()
                .scale(2.0F / captureSize)
                .translate(-1.0F, -1.0F)
                .scale(courseCaptureSize, -courseCaptureSize)
            ));
            final float coursePad = 2.0F;
            final float courseRange = courseCaptureSize + coursePad;

            final int curvatureCount = (int) (courseTrackPath.getLength() * 2.0F);
            final float[] curvature = new float[curvatureCount];
            for (int i = 0; i < curvatureCount; i++) {
                curvature[i] = courseTrackPath.getCurvature(i / (float) curvatureCount);
            }
            final float[] delta = new float[curvatureCount];
            for (int i = 0; i < curvatureCount; i++) {
                delta[i] = Math.signum(curvature[(i + 1) % curvatureCount] - curvature[i]);
            }
            final int[] sections = new int[curvatureCount];
            int sectionCount = 0;
            for (int i = 0; i < curvatureCount; i++) {
                if (delta[(i + 1) % curvatureCount] != delta[i]) {
                    sections[sectionCount++] = i + 1;
                }
            }
            final float[] weights = new float[sectionCount];
            int section = -1;
            float greatest = Float.NEGATIVE_INFINITY;
            int maxN = 0;
            for (int i = 0; i < sectionCount; i++) {
                final int start = sections[i];
                int end = sections[(i + 1) % sectionCount];
                if (end < start) {
                    end += curvatureCount;
                }
                if (end - start > maxN) {
                    maxN = end - start;
                }
            }
            for (int i = 0; i < sectionCount; i++) {
                final int start = sections[i];
                int end = sections[(i + 1) % sectionCount];
                if (end < start) {
                    end += curvatureCount;
                }
                final int n = end - start;
                final float K = curvature[start % curvatureCount];
                float Ex = 0.0F, Ex2 = 0.0F;
                for (int j = start; j < end; j++) {
                    final float x = curvature[j % curvatureCount];
                    Ex += x - K;
                    Ex2 += (x - K) * (x - K);
                }
                final float value = (n / (float) maxN) - 2.0F * ((Ex2 - (Ex * Ex) / n) / n);
                weights[i] = value;
                if (value > greatest) {
                    section = i;
                    greatest = value;
                }
            }
            final float finishLineT;
            final float trackDirection;
            {
                final int start = sections[section];
                int end = sections[(section + 1) % sectionCount];
                if (end < start) {
                    end += curvatureCount;
                }
                if (Math.abs(curvature[start % curvatureCount]) < Math.abs(curvature[(end - 1) % curvatureCount])) {
                    finishLineT = (start / (float) curvatureCount) % 1.0F;
                    trackDirection = 1.0F;
                } else {
                    finishLineT = (end / (float) curvatureCount) % 1.0F;
                    trackDirection = -1.0F;
                }
            }

            final float courseSize = 2.0F * courseRange;
            final int courseDiffuseSize = 2048;
            final float wallTileSize = 1.0F;
            final Bitmap courseDiffuse = Bitmap.createBitmap(courseDiffuseSize, courseDiffuseSize, Bitmap.Config.ARGB_8888);
            {
                final Bitmap pavementDiffuse = this.loadBitmap("materials/pavement_diffuse.png");
                final Bitmap finishLineDiffuse = this.loadBitmap("materials/finish_line_diffuse.png");
                final Canvas canvas = new Canvas(courseDiffuse);
                final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                final float courseToMap = courseDiffuseSize / courseSize;
                canvas.scale(courseToMap, -courseToMap);
                canvas.translate(courseRange, -courseRange);
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(this.createTileShader(pavementDiffuse, 1.0F));
                canvas.drawRect(-courseRange, -courseRange, courseRange, courseRange, paint);
                paint.setShader(null);
                // begin wall tile
                paint.setColor(0xFF2C2A30);
                canvas.drawRect(-courseRange, courseRange, -courseRange + wallTileSize, courseRange - wallTileSize, paint);
                // end wall tile
                final android.graphics.Path graphicsTrackPath = new android.graphics.Path();
                courseTrackPath.visit(new GraphicsPathVisitor(graphicsTrackPath));
                graphicsTrackPath.close();
                final float courseRoadMargin = 0.1F;
                final float courseRoadStripeWidth = 0.2F;
                {
                    final float outerRoadStripeWidth = courseRoadWidth - 2.0F * courseRoadMargin;
                    final float innerRoadStripeWidth = outerRoadStripeWidth - courseRoadStripeWidth;
                    final Bitmap roadStripDiffuse = Bitmap.createBitmap(courseDiffuseSize, courseDiffuseSize, Bitmap.Config.ARGB_8888);
                    final Canvas roadStripCanvas = new Canvas(roadStripDiffuse);
                    final Matrix m = new Matrix();
                    canvas.getMatrix(m);
                    roadStripCanvas.setMatrix(m);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(outerRoadStripeWidth);
                    paint.setColor(0xFFF2F3F4);
                    roadStripCanvas.drawPath(graphicsTrackPath, paint);
                    paint.setStrokeWidth(innerRoadStripeWidth);
                    paint.setColor(Color.TRANSPARENT);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    roadStripCanvas.drawPath(graphicsTrackPath, paint);
                    paint.setXfermode(null);
                    m.setScale(1.0F / courseToMap, -1.0F / courseToMap);
                    m.postTranslate(-courseRange, courseRange);
                    canvas.drawBitmap(roadStripDiffuse, m, null);
                }
                final Vector2 finishLineTranslation = courseTrackPath.getPoint(finishLineT);
                final Vector2 finishLineRotation = courseTrackPath.getTangent(finishLineT);
                paint.setColor(0xFFFFFFFF);
                paint.setStyle(Paint.Style.FILL);
                canvas.save();
                canvas.translate(finishLineTranslation.getX(), finishLineTranslation.getY());
                canvas.rotate(Mth.toDegrees(Mth.atan2(finishLineRotation.getY(), finishLineRotation.getX())) - 90.0F);
                final Shader shader = this.createTileShader(finishLineDiffuse, 1.0F);
                final Matrix m = new Matrix();
                shader.getLocalMatrix(m);
                m.postTranslate(0.0F, -0.5F);
                shader.setLocalMatrix(m);
                paint.setShader(shader);
                canvas.drawRect(-courseRoadHalfWidth, -0.5F, courseRoadHalfWidth, 0.5F, paint);
                paint.setShader(null);
                canvas.restore();

//                final android.graphics.Path strokePath = new android.graphics.Path();
//                PathStroker.stroke(courseTrackPath, (int) (courseTrackPath.getLength() * 0.5F), 2.5F).visit(new GraphicsPathVisitor(strokePath));
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(0.25F);
//                paint.setColor(0xFFFF0000);
//                canvas.drawPath(strokePath, paint);
            }
//            this.overlayView.setImageBitmap(courseDiffuse);
            Texture.builder().setSource(courseDiffuse).build().thenAccept(diffuse ->
                MaterialFactory.makeOpaqueWithTexture(MainActivity.this, diffuse).thenAccept(material -> {
                    final float roadHeight = 0.225F;
                    final Shape roadShape = new Shape();
                    roadShape.moveTo(0.0F, -courseRoadHalfWidth);
                    roadShape.lineTo(-roadHeight, -courseRoadHalfWidth);
                    roadShape.lineTo(-roadHeight, courseRoadHalfWidth);
                    roadShape.lineTo(0.0F, courseRoadHalfWidth);
                    roadShape.closePath();
                    final float wallHeight = 0.3F, wallWidth = 0.26F;
                    final Shape wallLeft = new Shape();
                    wallLeft.moveTo(0.0F, -courseRoadHalfWidth - wallWidth);
                    wallLeft.lineTo(-roadHeight - wallHeight, -courseRoadHalfWidth - wallWidth);
                    wallLeft.lineTo(-roadHeight - wallHeight, -courseRoadHalfWidth);
                    wallLeft.lineTo(0.0F, -courseRoadHalfWidth);
                    wallLeft.closePath();
                    final Shape wallRight = new Shape();
                    wallRight.moveTo(0.0F, courseRoadHalfWidth + wallWidth);
                    wallRight.lineTo(-roadHeight - wallHeight, courseRoadHalfWidth + wallWidth);
                    wallRight.lineTo(-roadHeight - wallHeight, courseRoadHalfWidth);
                    wallRight.lineTo(0.0F, courseRoadHalfWidth);
                    wallRight.closePath();
                    final CurvePath trackPath3 = this.toCurve3(courseTrackPath);
                    final ModelRenderable trackRenderable = Geometries.toRenderable(new ExtrudeGeometry(ImmutableList.of(roadShape), new ExtrudeGeometry.ExtrudeGeometryParameters() {{
                        this.steps = (int) (6 * trackPath3.getLength());
                        this.extrudePath = trackPath3;
                        this.uvGenerator = ExtrudeGeometry.ShapeUVGenerator.builder()
                            .setDefaultGenerator(ExtrudeGeometry.VertexUVGenerator.transform(new Matrix4()
                                .multiply(new Matrix4().makeTranslation(0.5F, 0.0F, 0.5F))
                                .multiply(new Matrix4().makeScale(1.0F / courseSize, 1.0F / courseSize, 1.0F / courseSize))
                            ))
                            .addShape(wallLeft, new ExtrudeGeometry.VertexUVGenerator(v -> new Vector3(0.0F, 0.0F, 1.0F)))
                            .addShape(wallRight, new ExtrudeGeometry.VertexUVGenerator(v -> new Vector3(0.0F, 0.0F, 1.0F)))
                            .build();
                    }}), material);
                    if (this.courseNode != null) {
                        view.getScene().removeChild(this.courseNode);
                    }
                    final Anchor anchor = scenePlane.createAnchor(planeAtPick);
                    this.courseNode = new AnchorNode(anchor);
                    this.courseNode.setLocalScale(com.google.ar.sceneform.math.Vector3.one().scaled(courseToSceneScale));
                    this.courseNode.setLocalPosition(new com.google.ar.sceneform.math.Vector3(0.0F, 0.01F, 0.0F));
                    this.courseNode.setParent(view.getScene());
                    final Node trackNode = new Node();
                    trackNode.setRenderable(trackRenderable);
                    trackNode.setParent(this.courseNode);
                    if (this.kart == null) {
                        this.kart = new KartModel(0, this.createKartDefinition());
                        this.kartNode = KartNode.create(this.kart, this.kartBody, this.kartWheel);
                    } else {
                        this.kart.reset();
                    }
                    final Node surfaceNode = new Node();
                    surfaceNode.setLocalPosition(new com.google.ar.sceneform.math.Vector3(0.0F, roadHeight, 0.0F));
                    surfaceNode.setParent(this.courseNode);
                    this.kartNode.setParent(surfaceNode);
                    final float kartT = finishLineT + 2.0F / courseTrackPath.getLength();
                    final Vector2 kartPos = courseTrackPath.getPoint(kartT);
                    final Vector2 kartRot = courseTrackPath.getTangent(kartT);
                    this.kart.position.copy(kartPos);
                    this.kart.rotation = Mth.atan2(kartRot.getY(), kartRot.getX()) - 0.5F * Mth.PI;
                    view.getPlaneRenderer().setVisible(false);
                }));
        }
    }

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

//    private void updateOverlayPath(final Bitmap capture, final Path pathInCapture) {
//        if (this.overlayView != null) {
//            final android.graphics.Path graphicPath = new android.graphics.Path();
//            pathInCapture.visit(new GraphicsPathVisitor(graphicPath));
//            graphicPath.close();
//            final Canvas canvas = new Canvas(capture);
//            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(3.0F);
//            paint.setColor(0x70FF1A52);
//            canvas.drawPath(graphicPath, paint);
//            paint.setColor(0xE0FF1A52);
//            // TODO: CurveVisitor
//            for (final Curve c : pathInCapture.getCurves()) {
//                final CubicBezierCurve cc = (CubicBezierCurve) c;
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(0xF0FF1A52);
//                canvas.drawCircle(cc.v0.getX(), cc.v0.getY(), 2.0F, paint);
//                paint.setColor(0x90FF1A52);
//                canvas.drawCircle(cc.v1.getX(), cc.v1.getY(), 1.5F, paint);
//                canvas.drawCircle(cc.v2.getX(), cc.v2.getY(), 1.5F, paint);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(1.0F);
//                canvas.drawLine(cc.v0.getX(), cc.v0.getY(), cc.v1.getX(), cc.v1.getY(), paint);
//                canvas.drawLine(cc.v3.getX(), cc.v3.getY(), cc.v2.getX(), cc.v2.getY(), paint);
//            }
//            this.overlayView.setImageBitmap(capture);
//        }
//    }

    private CurvePath toCurve3(final Path path) {
        final CurvePath curve3 = new CurvePath();
        path.visit(new PathVisitor() {
            private Vector3 last = new Vector3();

            @Override
            public void moveTo(final float x, final float y) {
                this.last = this.map(x, y);
            }

            @Override
            public void lineTo(final float x, final float y) {
                curve3.add(new LineCurve3(this.last, this.last = this.map(x, y)));
            }

            @Override
            public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
                curve3.add(new CubicBezierCurve3(this.last, this.map(aCP1x, aCP1y), this.map(aCP2x, aCP2y), this.last = this.map(aX, aY)));
            }

            @Override
            public void closePath() {}

            private Vector3 map(final float x, final float y) {
                return new Vector3(x, 0.0F, y);
            }
        });
        return curve3;
    }

    private static final class MissingBitmap {
        private static final Bitmap INSTANCE = Bitmap.createBitmap(
            new int[] {
                0xFF000000, 0xFFFF00FF,
                0xFFFF00FF, 0xFF000000
            }, 2, 2, Bitmap.Config.ARGB_8888
        );
    }

    private Bitmap loadBitmap(final String fileName) {
        final AssetManager assets = this.getAssets();
        Bitmap map = null;
        try (final InputStream is = assets.open(fileName)) {
            map = BitmapFactory.decodeStream(is);
            if (map == null) {
                Log.e(TAG, "Unable to decode bitmap '" + fileName + "'");
            }
        } catch (final IOException e) {
            Log.e(TAG, "Unable to read bitmap '" + fileName + "'", e);
        }
        if (map == null) {
            return MissingBitmap.INSTANCE;
        }
        return map;
    }

    private Shader createTileShader(final Bitmap bitmap, final float size) {
        final Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        final Matrix local = new Matrix();
        local.setScale(size / bitmap.getWidth(), size / bitmap.getHeight());
        shader.setLocalMatrix(local);
        return shader;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        final Game game = new Game();
//        this.controller = ServerController.create(SimpleServer.create(game, 20));
//        this.controller.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        this.controller.stop();
    }
}
