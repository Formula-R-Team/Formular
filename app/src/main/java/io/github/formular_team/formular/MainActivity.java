package io.github.formular_team.formular;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FormularActivity {
    private Button host, join;

    private Button customize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupButtons();
    }

    private void setupButtons() {
        this.host = this.findViewById(R.id.button_play);
        /*this.join = this.findViewById(R.id.button_join);*/
        this.customize = this.findViewById(R.id.button_customize);
        this.customize.setOnClickListener(this.activityChange(CustomizeActivity.class));
        this.host.setOnClickListener(v -> {
            final Intent intent = new Intent(this, SandboxActivity.class);
            intent.putExtra(SandboxActivity.EXTRA_HOST, true);
            this.startActivity(intent);
        });
        /*this.join.setOnClickListener(v -> {
            final Intent intent = new Intent(this, SandboxActivity.class);
            intent.putExtra(SandboxActivity.EXTRA_HOST, false);
            this.startActivity(intent);
        });*/
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}
