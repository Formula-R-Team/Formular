package io.github.formular_team.formular;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.Kart;

public final class KartController implements View.OnTouchListener {
    private final Kart kart;

    private final View pad;

    private final View wheel;

    public KartController(final Kart kart, final View pad, final View wheel) {
        this.kart = kart;
        this.pad = pad;
        this.wheel = wheel;
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
            this.kart.getControlState().setSteeringAngle(angle);
            this.kart.getControlState().setThrottle(-y * 40.0F);
            this.kart.getControlState().setBrake(0.0F);
            this.wheel.setRotation(-Mth.toDegrees(angle));
            return true;
        case MotionEvent.ACTION_UP:
            this.kart.getControlState().setThrottle(0.0F);
            this.kart.getControlState().setBrake(100.0F);
            return true;
        }
        return false;
    }
}
