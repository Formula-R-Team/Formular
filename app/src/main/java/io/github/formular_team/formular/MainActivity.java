package io.github.formular_team.formular;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FormularActivity {
    private Button hostRace, joinRace;

    private Button customize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupButtons();
    }

    private void setupButtons() {
        this.hostRace = this.findViewById(R.id.btnHostRace);
        this.joinRace = this.findViewById(R.id.btnJoinRace);
        this.customize = this.findViewById(R.id.btnCustomize);
        /*this.findViewById(R.id.search).setOnClickListener(this.activityChange(RaceActivity.class));*/
        this.customize.setOnClickListener(this.activityChange(ShopActivity.class));
        this.hostRace.setOnClickListener(v -> {
            final Intent intent = new Intent(this, SandboxActivity.class);
            intent.putExtra(SandboxActivity.EXTRA_HOST, true);
            this.startActivity(intent);
        });
        this.joinRace.setOnClickListener(v -> {
            final Intent intent = new Intent(this, SandboxActivity.class);
            intent.putExtra(SandboxActivity.EXTRA_HOST, false);
            this.startActivity(intent);
        });
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}
