package io.github.formular_team.formular;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.kart.Kart;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.math.Mth;

public class SteeringWheelView extends View {
    private Drawable wheel;

    private int wheelWidth;

    private int wheelHeight;

    private boolean sizeChanged = true;

    private float angle = 0.0F;

    private SteerListener listener;

    private Kart.ControlState state = new SimpleControlState();

    public SteeringWheelView(final Context context) {
        this(context, null);
    }

    public SteeringWheelView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SteeringWheelView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SteeringWheelView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SteeringWheelView, defStyleAttr, defStyleRes);
        try {
            this.wheel = a.getDrawable(R.styleable.SteeringWheelView_wheel);
            if (this.wheel == null) {
                this.wheel = context.getDrawable(R.drawable.steering_wheel);
            }
            if (this.wheel == null) {
                throw new NullPointerException();
            }
            this.wheelWidth = this.wheel.getIntrinsicWidth();
            this.wheelHeight = this.wheel.getIntrinsicHeight();
        } finally {
            a.recycle();
        }
    }

    public void setAngle(final float angle) {
        this.angle = angle;
        this.invalidate();
    }

    public float getAngle() {
        return this.angle;
    }

    public void setSteerListener(final SteerListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            final float x = Mth.clamp(event.getX() / this.getWidth() * 2.0F - 1.0F, -1.0F, 1.0F);
            final float y = Mth.clamp(event.getY() / this.getHeight() * 2.0F - 1.0F, -1.0F, 1.0F);
            final float angle = -Mth.PI / 4.0F * x;
            this.state.setSteeringAngle(angle);
            this.state.setThrottle(-y * 6.5F);
            this.state.setBrake(0.0F);
            if (this.listener != null) {
                this.listener.onSteer(this.state);
            }
            this.setAngle(-Mth.toDegrees(angle));
            return true;
        case MotionEvent.ACTION_UP:
            this.state.setThrottle(0.0F);
            this.state.setBrake(10.0F);
            if (this.listener != null) {
                this.listener.onSteer(this.state);
            }
            return true;
        }
        return false;
    }

    public interface SteerListener {
        void onSteer(final KartModel.ControlState state);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final float hScale = widthMode != MeasureSpec.UNSPECIFIED && widthSize < this.wheelWidth ? (float) widthSize / this.wheelWidth : 1.0F;
        final float vScale = heightMode != MeasureSpec.UNSPECIFIED && widthSize < this.wheelHeight ? (float) heightSize / this.wheelHeight : 1.0F;
        final float scale = Math.min(hScale, vScale);
        final int measuredWidth = resolveSizeAndState((int) (this.wheelWidth * scale), widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState((int) (this.wheelHeight * scale), heightMeasureSpec, 0);
        this.setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.sizeChanged = true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        final boolean changed = this.sizeChanged;
        if (changed) {
            this.sizeChanged = false;
        }
        final int availableWidth = this.getWidth();
        final int availableHeight = this.getHeight();
        final int centerX = availableWidth / 2;
        final int centerY = availableHeight / 2;
        canvas.save();
        if (availableWidth < this.wheelWidth || availableHeight < this.wheelHeight) {
            final float scale = Math.min((float) availableWidth / this.wheelWidth, (float) availableHeight / this.wheelHeight);
            canvas.scale(scale, scale, centerX, centerY);
        }
        if (changed) {
            final int extentX = this.wheelWidth / 2;
            final int extentY = this.wheelHeight / 2;
            this.wheel.setBounds(centerX - extentX, centerY - extentY, centerX + extentX, centerY + extentY);
        }
        canvas.rotate(this.angle, centerX, centerY);
        this.wheel.draw(canvas);
        canvas.restore();
    }
}
