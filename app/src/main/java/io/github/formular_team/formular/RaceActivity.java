package io.github.formular_team.formular;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.ar.CourseNode;
import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.ar.LabelFactory;
import io.github.formular_team.formular.ar.ModelLoader;
import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.Checkpoint;
import io.github.formular_team.formular.core.Course;
import io.github.formular_team.formular.core.CourseMetadata;
import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.GameModel;
import io.github.formular_team.formular.core.KartDefinition;
import io.github.formular_team.formular.core.KartModel;
import io.github.formular_team.formular.core.SimpleDriver;
import io.github.formular_team.formular.core.SimpleGameModel;
import io.github.formular_team.formular.core.SimpleTrackFactory;
import io.github.formular_team.formular.core.Track;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.math.Bezier;
import io.github.formular_team.formular.core.math.LineCurve;
import io.github.formular_team.formular.core.math.Matrix3;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Path;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.race.RaceConfiguration;
import io.github.formular_team.formular.core.race.RaceListener;
import io.github.formular_team.formular.core.tracing.CirclePathLocator;
import io.github.formular_team.formular.core.tracing.OrientFunction;
import io.github.formular_team.formular.core.tracing.PathFinder;
import io.github.formular_team.formular.core.tracing.SimplePathTracer;
import io.github.formular_team.formular.core.tracing.SimpleStepFunction;

public class RaceActivity extends FormularActivity implements ModelLoader.Listener {
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

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setRenderable(final int id, final ModelRenderable modelRenderable) {
        if (id == KART_BODY) {
            this.kartBody = modelRenderable;
        } else if (id == KART_WHEEL) {
            this.kartWheel = modelRenderable;
        }
    }

    @Override
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
        final float captureRange = 0.25F;
        final int captureSize = 200;
        final Bitmap rectifiedCapture;
        try (final Rectifier rectifier = new Rectifier(arFrame)) {
            final Matrix model = new Matrix();
            hitResult.getHitPose().toMatrix(model.data, 0);
            final Matrix rangeScale = new Matrix();
            rangeScale.makeScale(captureRange);
            Matrix.multiply(model, rangeScale, model);
            rectifiedCapture = rectifier.rectify(model.data, captureSize);
        } catch (final NotYetAvailableException e) {
            throw new AssertionError();
        }
        final float courseRoadWidth = 6.0F;
        final float courseToSceneScale = 0.05F / courseRoadWidth;
        final float courseCaptureSize = captureRange / courseToSceneScale;
        final Path linePath = new Path();
        new PathFinder(
                new CirclePathLocator(25),
                new SimplePathTracer(
                    new SimpleStepFunction(7, (0.5F * Mth.PI)),
                    new OrientFunction(3)
                )
            )
            .find(new BitmapImageMap(rectifiedCapture))
            .visit(new TransformingPathVisitor(linePath, new Matrix3()
                .scale(2.0F / captureSize)
                .translate(-1.0F, -1.0F)
                .scale(courseCaptureSize, -courseCaptureSize)
            ));
        final Path path = Bezier.fitCurve(linePath, 0.175F);
        if (path.getLength() == 0.0F || !path.isClosed()) {
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
        final Track track = new SimpleTrackFactory(courseRoadWidth).create(path);
        final float coursePad = 2.0F;
        final float courseRange = courseCaptureSize + coursePad;
        final float courseSize = 2.0F * courseRange;
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
        final List<? extends Checkpoint> checkpoints = track.getCheckpoints();
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
}
