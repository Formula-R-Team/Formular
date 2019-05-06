package io.github.formular_team.formular.ar;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.Color;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.formular_team.formular.KartNodeFactory;
import io.github.formular_team.formular.R;
import io.github.formular_team.formular.core.Course;
import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.KartDefinition;
import io.github.formular_team.formular.core.KartView;
import io.github.formular_team.formular.core.RacerStatus;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.StateKartView;
import io.github.formular_team.formular.core.math.Vector2;

public class ArGameView implements GameView {
    private final Activity activity;

    private final TextView countText, positionText, lapText;

    private final Scene scene;

    private final Node surface;

    private final KartNodeFactory factory;

    private final SparseArray<KartView> karts;

    private final Kart.ControlState controlState = new SimpleControlState();

    private ArGameView(final Activity activity, final TextView countText, final TextView positionText, final TextView lapText, final Scene scene, final Node surface, final KartNodeFactory factory, final SparseArray<KartView> karts) {
        this.activity = activity;
        this.countText = countText;
        this.positionText = positionText;
        this.lapText = lapText;
        this.scene = scene;
        this.surface = surface;
        this.factory = factory;
        this.karts = karts;
    }

    @Override
    public RacerStatus getStatus() {
        return new RacerStatus();
    }

    @Override
    public Kart createKart(final int uniqueId, final io.github.formular_team.formular.core.color.Color color, final Vector2 position, final float rotation) {
        final KartView kart = new StateKartView(uniqueId, KartDefinition.createKart2(), position, rotation);
        this.activity.runOnUiThread(() -> {
            final KartNode kn = this.factory.create(kart);
            kn.setColor(new Color(color.getHex()));
            if (this.pendingCourse != null) {
                this.pendingCourse.thenAccept(n -> n.add(kn));
            } else {
                this.surface.addChild(kn);
            }
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
        this.activity.runOnUiThread(() -> {
            this.countText.setText(this.getCountResource(count));
            final Animation anim = new AlphaAnimation(1.0F, 0.0F);
            anim.setDuration(1000);
            anim.setFillEnabled(true);
            anim.setFillAfter(true);
            this.countText.startAnimation(anim);
        });
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
            this.lapText.setText(this.activity.getString(R.string.race_lap, Math.min(1 + lap, lapCount), lapCount));
        });
    }

    @Override
    public void setPosition(final int position) {
        this.activity.runOnUiThread(() ->
            this.positionText.setText(this.activity.getString(this.getPositionResource(position), position))
        );
    }

    @Override
    public void onFinish() {
        this.activity.runOnUiThread(() -> {
            this.countText.setText(R.string.race_finish);
            final Animation anim = new AlphaAnimation(1.0F, 0.0F);
            anim.setStartOffset(1200);
            anim.setDuration(1000);
            anim.setFillEnabled(true);
            anim.setFillBefore(true);
            anim.setFillAfter(true);
            this.countText.startAnimation(anim);
        });
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

    public static ArGameView create(final Activity activity, final TextView countText, final TextView positionText, final TextView lapText, final Scene scene, final Node surface, final KartNodeFactory factory) {
        return new ArGameView(activity, countText, positionText, lapText, scene, surface, factory, new SparseArray<>());
    }
}
