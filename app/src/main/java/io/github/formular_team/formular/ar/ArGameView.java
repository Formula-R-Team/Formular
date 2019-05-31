package io.github.formular_team.formular.ar;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.util.SparseArray;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.formular_team.formular.KartNodeFactory;
import io.github.formular_team.formular.R;
import io.github.formular_team.formular.RaceView;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.StateKartView;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.game.GameView;
import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.kart.KartDefinition;
import io.github.formular_team.formular.core.kart.KartView;
import io.github.formular_team.formular.core.math.Vector2;

public class ArGameView implements GameView {
    private final Activity activity;

    private final RaceView overlay;

    private final Scene scene;

    private final Node surface;

    private final KartNodeFactory factory;

    private final SparseArray<KartView> karts;

    private final Kart.ControlState controlState = new SimpleControlState();

    private ArGameView(final Activity activity, final RaceView overlay, final Scene scene, final Node surface, final KartNodeFactory factory, final SparseArray<KartView> karts) {
        this.activity = activity;
        this.overlay = overlay;
        this.scene = scene;
        this.surface = surface;
        this.factory = factory;
        this.karts = karts;
    }

    @Override
    public Kart createKart(final int uniqueId, final io.github.formular_team.formular.core.color.Color color, final Vector2 position, final float rotation, final User user) {
        final KartView kart = new StateKartView(uniqueId, KartDefinition.createKart2(), position, rotation);
        this.activity.runOnUiThread(() -> {
            final KartNode kn = this.factory.create(kart);
            kn.setColor(new Color(color.getHex()));
            if (this.pendingCourse != null) {
                this.pendingCourse.thenAccept(n -> n.add(kn));
            } else {
                this.surface.addChild(kn);
            }
            LabelFactory.create(this.activity, user.getName(), 1.0F).thenAccept(n -> {
                n.setLocalPosition(Vector3.up().scaled(2.2F));
                kn.addChild(n);
            });
        });
        this.karts.put(uniqueId, kart);
        return kart;
    }

    private CompletableFuture<CourseNode> pendingCourse; // FIXME: course id mapping

    @Override
    public void addCourse(final Course course) {
        this.activity.runOnUiThread(() -> {
            this.pendingCourse = CourseNode.create(this.activity, course);
            this.pendingCourse.thenAccept(this.surface::addChild);
        });
    }

    @Override
    public void setCount(final int count) {
        this.activity.runOnUiThread(() -> this.overlay.setCount(this.getCountResource(count)));
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
    public void setLap(final int lap) {
        this.activity.runOnUiThread(() -> {
            final int lapCount = 3/*race.getConfiguration().getLapCount()*/; // FIXME: lap count
            this.overlay.setLap(R.string.race_lap, Math.min(1 + lap, lapCount), lapCount);
        });
    }

    @Override
    public void setPosition(final int position) {
        this.activity.runOnUiThread(() -> this.overlay.setPosition(this.getPositionResource(position), position));
    }

    @Override
    public void onFinish() {
        this.activity.runOnUiThread(this.overlay::setFinish);
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
    public Optional<Kart> removeKart(final int uniqueId) {
        final Kart kart = this.karts.get(uniqueId);
        if (kart == null) {
            return Optional.empty();
        }
        this.karts.remove(uniqueId);
        return Optional.of(kart);
    }

    @Override
    public Optional<Kart> getKart(final int uniqueId) {
        return Optional.ofNullable(this.karts.get(uniqueId));
    }

    @Override
    public Kart.ControlState getControlState() {
        return this.controlState;
    }

    public static ArGameView create(final Activity activity, final RaceView overlay, final Scene scene, final Node surface, final KartNodeFactory factory) {
        return new ArGameView(activity, overlay, scene, surface, factory, new SparseArray<>());
    }
}
