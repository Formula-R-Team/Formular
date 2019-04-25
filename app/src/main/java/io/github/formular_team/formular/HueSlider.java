package io.github.formular_team.formular;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class HueSlider extends AppCompatSeekBar {
    public HueSlider(final Context context) {
        super(context);
    }

    public HueSlider(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.seekBarStyle);
    }

    public HueSlider(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
