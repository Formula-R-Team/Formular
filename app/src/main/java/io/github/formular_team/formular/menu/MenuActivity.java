package io.github.formular_team.formular.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import io.github.formular_team.formular.Activities;
import io.github.formular_team.formular.CustomizeActivity;
import io.github.formular_team.formular.MainActivity;
import io.github.formular_team.formular.R;

public class MenuActivity extends AppCompatActivity {
    private Button btnRace;

    private Button btnCustomize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_menu);
        Activities.makeFullscreen(this);
        this.setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Activities.makeFullscreen(this);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Activities.makeFullscreen(this);
        }
    }

    private void setupButtons() {
        this.btnRace = this.findViewById(R.id.btnRace);
        this.btnCustomize = this.findViewById(R.id.btnCustomize);
        this.btnCustomize.setOnClickListener(this.activityChange(CustomizeActivity.class));
        this.btnRace.setOnClickListener(this.activityChange(MainActivity.class));
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}
