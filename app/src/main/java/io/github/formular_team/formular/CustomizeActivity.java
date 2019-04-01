package io.github.formular_team.formular;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CustomizeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_customize);
        Activities.makeFullscreen(this);
    }
}
