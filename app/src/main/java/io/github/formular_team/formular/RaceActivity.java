package io.github.formular_team.formular;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.opengl.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
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
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.github.formular_team.formular.ar.scene.CourseNode;
import io.github.formular_team.formular.ar.scene.KartNode;
import io.github.formular_team.formular.math.Bezier;
import io.github.formular_team.formular.math.LineCurve;
import io.github.formular_team.formular.math.Matrix3;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.PathOffset;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.TransformingPathVisitor;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;
import io.github.formular_team.formular.server.Checkpoint;
import io.github.formular_team.formular.server.Course;
import io.github.formular_team.formular.server.CourseMetadata;
import io.github.formular_team.formular.server.Driver;
import io.github.formular_team.formular.server.FinishLineOptimizer;
import io.github.formular_team.formular.server.KartDefinition;
import io.github.formular_team.formular.server.KartModel;
import io.github.formular_team.formular.server.SimpleDriver;
import io.github.formular_team.formular.server.SimpleGameModel;
import io.github.formular_team.formular.server.Track;
import io.github.formular_team.formular.server.race.Race;
import io.github.formular_team.formular.server.race.RaceConfiguration;
import io.github.formular_team.formular.server.race.RaceListener;
import io.github.formular_team.formular.trace.BilinearMapper;
import io.github.formular_team.formular.trace.ImageLineMap;
import io.github.formular_team.formular.trace.OrientFunction;
import io.github.formular_team.formular.trace.PathReader;
import io.github.formular_team.formular.trace.SimpleStepFunction;
import io.github.formular_team.formular.trace.TransformMapper;

public class RaceActivity extends FormularActivity {
    private static final String TAG = RaceActivity.class.getSimpleName();

    private ModelLoader modelLoader;

    private static final int KART_BODY = 0, KART_WHEEL = 1;

    private ModelRenderable kartBody, kartWheel;

    private Node courseAnchor;

    private GameModel game;

    private KartModel kart;

    private User user;

    private TextView lapView, positionView, countView;

    private View pad, wheel;

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
        this.setContentView(R.layout.activity_race);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String namePref = prefs.getString("prefName", "Player 1");
        final int colorPref = prefs.getInt("prefColor", 0xFFFF007F);
        this.user = User.create(namePref, colorPref);
        this.lapView = this.findViewById(R.id.lap);
        this.positionView = this.findViewById(R.id.position);
        this.countView = this.findViewById(R.id.count);
        this.pad = this.findViewById(R.id.pad);
        this.wheel = this.findViewById(R.id.wheel);
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        this.modelLoader = new ModelLoader(this);
        this.modelLoader.loadModel(KART_BODY, R.raw.kart);
        this.modelLoader.loadModel(KART_WHEEL, R.raw.wheel);
        this.arFragment.setOnTapArPlaneListener(this::onPlaneTap);
        this.findViewById(R.id.reset).setOnClickListener(v -> {
            if (this.courseAnchor != null) {
                final ArSceneView view = this.arFragment.getArSceneView();
                view.getScene().removeChild(this.courseAnchor);
                view.getPlaneRenderer().setVisible(true);
            }
            this.game = null;
        });
        this.arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (this.game == null) {
                return;
            }
            final float targetDt = 0.01F;
            final float delta = frameTime.getDeltaSeconds();
            final int steps = Math.max((int) (delta / targetDt), 1);
            final float dt = delta / steps;
            for (int n = 0; n < steps; n++) {
                this.game.step(dt);
            }
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
        final Frame arFrame = view.getArFrame();
        if (arFrame == null) {
            throw new AssertionError();
        }
        final Camera camera = arFrame.getCamera();
        final float[] projMat = new float[16];
        camera.getProjectionMatrix(projMat, 0, 0.1F, 10.0F);
        final float[] viewMat = new float[16];
        camera.getViewMatrix(viewMat, 0);
        final float[] modelMat = new float[16];
        final Pose po = hitResult.getHitPose();
        po.toMatrix(modelMat, 0);
        final float[] viewProjMat = new float[16];
        final float[] mvp = new float[16];
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0);
        Matrix.multiplyMM(mvp, 0, viewProjMat, 0, modelMat, 0);
        final float captureRange = 0.25F;
        final float[] xyz0 = new float[4], xyz1 = new float[4];
        final Function<Vector3, Vector2> planeToImage = in -> {
            xyz0[0] = in.getX();
            xyz0[1] = in.getY();
            xyz0[2] = in.getZ();
            xyz0[3] = 1.0F;
            Matrix.multiplyMV(xyz1, 0, mvp, 0, xyz0, 0);
            final float d = 1.0F / xyz1[3];
            xyz1[0] *= d;
            xyz1[1] *= d;
            arFrame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, xyz1,
                Coordinates2d.IMAGE_PIXELS, xyz0
            );
            return new Vector2(xyz0[0], xyz0[1]);
        };
        final Vector2 b00 = planeToImage.apply(new Vector3(-captureRange, 0.0F, captureRange));
        final Vector2 b01 = planeToImage.apply(new Vector3(-captureRange, 0.0F, -captureRange));
        final Vector2 b10 = planeToImage.apply(new Vector3(captureRange, 0.0F, captureRange));
        final Vector2 b11 = planeToImage.apply(new Vector3(captureRange, 0.0F, -captureRange));
        final Vector2 min, max;
        final Bitmap image;
        final int captureSize = 200; //used to be 128
        try (final Image cameraImage = arFrame.acquireCameraImage()) {
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
        // TODO: path failure feedback
        final Path captureSegments = this.findPath(capture);
        if (captureSegments.getLength() == 0.0F || !captureSegments.isClosed()) {
            Log.v(TAG, "Curve not continuous");
            this.countView.setText("!");
            final Animation anim = new AlphaAnimation(1.0F, 0.0F);
            anim.setStartOffset(1500);
            anim.setDuration(1000);
            anim.setFillEnabled(true);
            anim.setFillBefore(true);
            anim.setFillAfter(true);
            this.countView.startAnimation(anim);
            return;
        }
        final Path captureTrackPath = Bezier.fitBezierCurve(captureSegments, 8.0F);
        final float courseRoadWidth = 6.0F;
//                this.updateOverlayPath(capture, captureTrackPath);
        final Path courseTrackPath = new Path();
        final float courseToSceneScale = 0.05F / courseRoadWidth;
        final float courseCaptureSize = captureRange / courseToSceneScale;
        captureTrackPath.visit(new TransformingPathVisitor(courseTrackPath, new Matrix3()
            .scale(2.0F / captureSize)
            .translate(-1.0F, -1.0F)
            .scale(courseCaptureSize, -courseCaptureSize)
        ));

        final float finishLinePosition = new FinishLineOptimizer().get(courseTrackPath);
        final List<PathOffset.Frame> frames = PathOffset.createFrames(courseTrackPath, finishLinePosition, (int) (courseTrackPath.getLength() * 0.75F), courseRoadWidth + 0.75F);

        final int requiredCheckPointCount = 8;
        final ImmutableList.Builder<Checkpoint> bob = ImmutableList.builder();
        final int requiredInterval = frames.size() / requiredCheckPointCount;
        for (int i = 0; i < frames.size(); i++) {
            final PathOffset.Frame fm = frames.get(i);
            bob.add(new Checkpoint(fm.getP1(), fm.getP2(), i, fm.getT(), frames.size() - i > requiredInterval && i % requiredInterval == 0));
        }
        final ImmutableList<Checkpoint> checkpoints = bob.build();
        final float coursePad = 2.0F;
        final float courseRange = courseCaptureSize + coursePad;
        final float courseSize = 2.0F * courseRange;
        final Track track = Track.builder()
                .setRoadPath(courseTrackPath)
                .setRoadWidth(courseRoadWidth)
                .setRoadShape(new Shape()) // TODO: road shape
                .setCheckpoints(checkpoints)
                .build();
        final Course course = Course.builder()
                .setMetadata(CourseMetadata.create(this.user, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "My Circuit"))
                .setSize(courseSize)
                .setTrack(track)
                .build();
        if (course.getTrack().getCheckpoints().size() < 5) {
            Log.v(TAG, "Curve too short");
            this.countView.setText("!!");
            final Animation anim = new AlphaAnimation(1.0F, 0.0F);
            anim.setStartOffset(1500);
            anim.setDuration(1000);
            anim.setFillEnabled(true);
            anim.setFillBefore(true);
            anim.setFillAfter(true);
            this.countView.startAnimation(anim);
            return;
        }
        this.game = new SimpleGameModel();
        for (int i = 0; i < checkpoints.size(); i++) {
            this.game.getWalls().add(new LineCurve(checkpoints.get(i).getP1(), checkpoints.get((i + 1) % checkpoints.size()).getP1()));
            this.game.getWalls().add(new LineCurve(checkpoints.get(i).getP2(), checkpoints.get((i + 1) % checkpoints.size()).getP2()));
        }
        this.kart = new KartModel(this.game, 0, this.createKartDefinition());
        this.pad.setOnTouchListener(new KartController(this.kart, this.pad, this.wheel));
        final Driver self = SimpleDriver.create(this.user, this.kart);
        final Race race = Race.create(this.game, RaceConfiguration.create(3), this.user, course);
        race.addListener(new RaceListener() {
            int lap, position;
            float progress;
            boolean wrongWay;

            @Override
            public void onBegin() {}

            @Override
            public void onEnd() {
                if (this.position == 0) {
                    RaceActivity.this.countView.setText(R.string.race_finish);
                    final Animation anim = new AlphaAnimation(1.0F, 0.0F);
                    anim.setStartOffset(1200);
                    anim.setDuration(1000);
                    anim.setFillEnabled(true);
                    anim.setFillBefore(true);
                    anim.setFillAfter(true);
                    RaceActivity.this.countView.startAnimation(anim);
                }
            }

            @Override
            public void onCount(final int count) {
                RaceActivity.this.countView.setText(this.getCountResource(count));
                final Animation anim = new AlphaAnimation(1.0F, 0.0F);
                anim.setDuration(1000);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                RaceActivity.this.countView.startAnimation(anim);
            }

            @StringRes
            private int getCountResource(final int count) {
                switch (count) {
                case 0:
                    return R.string.race_count_0;
                case 1:
                    return R.string.race_count_1;
                case 2:
                    return R.string.race_count_2;
                case 3:
                    return R.string.race_count_3;
                default:
                    return 0;
                }
            }

            @Override
            public void onProgress(final Driver driver, final float progress) {
                if (self.equals(driver)) {
                    this.progress = progress;
                }
            }

            @Override
            public void onPosition(final Driver driver, final int position) {
                if (self.equals(driver)) {
                    this.position = position;
                    RaceActivity.this.positionView.setText(RaceActivity.this.getString(this.getPositionResource(position), position));
                }
            }

            @StringRes
            private int getPositionResource(final int position) {
                switch (position) {
                case 0:
                    return R.string.race_position_0;
                case 1:
                    return R.string.race_position_1;
                case 2:
                    return R.string.race_position_2;
                default:
                    return R.string.race_position_default;
                }
            }

            @Override
            public void onLap(final Driver driver, final int lap) {
                if (self.equals(driver)) {
                    this.lap = lap;
                    final int lapCount = race.getConfiguration().getLapCount();
                    RaceActivity.this.lapView.setText(RaceActivity.this.getString(R.string.race_lap, Math.min(1 + this.lap, lapCount), lapCount));
                }
            }

            @Override
            public void onForward(final Driver driver) {
                if (self.equals(driver)) {
                    this.wrongWay = false;
                }
            }

            @Override
            public void onReverse(final Driver driver) {
                if (self.equals(driver)) {
                    this.wrongWay = true;
                }
            }
        });
        this.game.addRace(race);
        this.game.addKart(this.kart);
        this.game.addDriver(self);
        race.add(self);

        // Add cpus
//            final ColorPalette cpuColors = SimplePaletteFactory.builder()
//                    .color(SimpleColorRange.builder()
//                            .saturation(Range.closedOpen(0.5F, 0.95F))
//                            .value(Range.closedOpen(0.5F, 1.0F))
//                            .build()
//                    )
//                    .size(Range.singleton(3))
//                    .build()
//                    .create(new Random());
//            for (int n = 0; n < cpuColors.size(); n++) {
//                final KartModel kart = new KartModel(this.game, 1 + n, this.createKartDefinition());
//                this.game.addKart(kart);
//                final Driver driver = CpuDriver.create(User.create("CPU #" + (1 + n), cpuColors.get(n)), kart);
//                this.game.addDriver(driver);
//                race.add(driver);
//            }
        race.start();

        CourseNode.create(RaceActivity.this, course).thenAccept(courseNode -> {
            if (this.courseAnchor != null) {
                view.getScene().removeChild(this.courseAnchor);
            }
            view.getPlaneRenderer().setVisible(false);
            final Anchor anchor = hitResult.createAnchor();//scenePlane.createAnchor(hitResult.getHitPose());
            this.courseAnchor = new AnchorNode(anchor);
            this.courseAnchor.setLocalScale(com.google.ar.sceneform.math.Vector3.one().scaled(courseToSceneScale));
            this.courseAnchor.setLocalPosition(new com.google.ar.sceneform.math.Vector3(0.0F, 0.01F, 0.0F));
            this.courseAnchor.addChild(courseNode);
            view.getScene().addChild(this.courseAnchor);

            for (final Driver driver : this.game.getDrivers()) {
                final ModelRenderable body = this.kartBody.makeCopy();
                body.getMaterial(0).setFloat4("baseColor", new Color(0xFF000000 | driver.getUser().getColor()));
                final KartNode kart = KartNode.create(driver.getVehicle(), body, this.kartWheel);
                LabelFactory.create(this, driver.getUser() == this.user ? "YOU" : driver.getUser().getName(), 1.5F)
                        .thenAccept(label -> {
                            label.setLocalPosition(com.google.ar.sceneform.math.Vector3.up().scaled(1.8F));
                            kart.addChild(label);
                        });
                courseNode.add(kart);
            }
        });
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}