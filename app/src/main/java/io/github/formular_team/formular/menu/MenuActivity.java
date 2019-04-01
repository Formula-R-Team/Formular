package io.github.formular_team.formular.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.github.formular_team.formular.CustomizeActivity;
import io.github.formular_team.formular.FormularActivity;
import io.github.formular_team.formular.MainActivity;
import io.github.formular_team.formular.R;

public class MenuActivity extends FormularActivity {
    private Button btnRace;

    private Button btnCustomize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
        this.setupButtons();
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
