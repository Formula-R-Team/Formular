package io.github.formular_team.formular;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FormularActivity {
    private Button btnRace;

    private Button btnCustomize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupButtons();
    }

    private void setupButtons() {
        this.btnRace = this.findViewById(R.id.btnRace);
        this.btnCustomize = this.findViewById(R.id.btnCustomize);
        this.btnCustomize.setOnClickListener(this.activityChange(ShopActivity.class));
        this.btnRace.setOnClickListener(v -> {
            final Intent intent = new Intent(this, SandboxActivity.class);
            intent.putExtra(SandboxActivity.EXTRA_HOST, false);
            this.startActivity(intent);
        });
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}
