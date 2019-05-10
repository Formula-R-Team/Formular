package io.github.formular_team.formular;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import java.util.function.Consumer;

import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.math.Mth;

public final class KartController implements View.OnTouchListener {
    private final Kart.ControlState state;

    private final Consumer<Kart.ControlState> listener;

    public KartController(final Kart.ControlState state, final Consumer<Kart.ControlState> listener) {
        this.state = state;
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // TODO: flip x/y if landscape orientation
                final float x = Mth.clamp(event.getX() / v.getWidth() * 2.0F - 1.0F, -1.0F, 1.0F);
                final float y = Mth.clamp(event.getY() / v.getHeight() * 2.0F - 1.0F, -1.0F, 1.0F);
                final float angle = -Mth.PI / 4.0F * x;
                this.state.setSteeringAngle(angle);
                this.state.setThrottle(-y * 7.5F);
                this.state.setBrake(0.0F);
                this.listener.accept(this.state);
                ((SteeringWheelView) v).setAngle(-Mth.toDegrees(angle));
                return true;
            case MotionEvent.ACTION_UP:
                this.state.setThrottle(0.0F);
                this.state.setBrake(10.0F);
                this.listener.accept(this.state);
                return true;
        }
        return false;
    }
}
