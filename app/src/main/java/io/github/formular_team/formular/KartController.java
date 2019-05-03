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

    private final View wheel;

    public KartController(final Kart.ControlState state, final Consumer<Kart.ControlState> listener, final View wheel) {
        this.state = state;
        this.listener = listener;
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
                this.state.setSteeringAngle(angle);
                this.state.setThrottle(-y * 4.0F);
                this.state.setBrake(0.0F);
                this.listener.accept(this.state);
                //this.wheel.setRotation(-Mth.toDegrees(angle));

                this.wheel.setTop((int) event.getY() + v.getTop() - 150);
                if(this.wheel.getTop() < v.getTop()){
                    this.wheel.setTop(v.getTop());
                }
                this.wheel.setLeft((int) event.getX() + v.getLeft() - 150);
                if(this.wheel.getLeft() < v.getLeft()){
                    this.wheel.setLeft(v.getLeft());
                }
                this.wheel.setBottom(this.wheel.getTop() + 300);
                if(this.wheel.getBottom() > v.getBottom()){
                    this.wheel.setBottom(v.getBottom());
                    this.wheel.setTop(v.getBottom() - 300);
                }
                this.wheel.setRight(this.wheel.getLeft() + 300);
                if(this.wheel.getRight() > v.getRight()){
                    this.wheel.setRight(v.getRight());
                    this.wheel.setLeft(v.getRight() - 300);
                }
                return true;

            case MotionEvent.ACTION_UP:
                this.state.setThrottle(0.0F);
                this.state.setBrake(10.0F);
                this.listener.accept(this.state);

                //reset joystick to center
                this.wheel.setTop(v.getTop() + 170);
                this.wheel.setLeft(v.getLeft() + 170);
                this.wheel.setBottom(v.getTop() + 470);
                this.wheel.setRight(v.getLeft() + 470);
                return true;

        }

        return false;
    }
}
