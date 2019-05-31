package io.github.formular_team.formular;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends FormularActivity {
    private Button host, join;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupButtons();
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Enter name")
            .setView(R.layout.host_address_prompt)
            .create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (d, which) -> {
            final EditText addressText = dialog.findViewById(R.id.host_address);
            if (addressText != null) {
                final String text = addressText.getText().toString();
                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                settings.edit().putString("user.name", text).apply();
            }
        });
        dialog.show();
    }

    private void setupButtons() {
        this.host = this.findViewById(R.id.button_play);
        /*this.join = this.findViewById(R.id.button_join);*/
        this.host.setOnClickListener(v -> {
            this.startActivity(new Intent(this, SandboxActivity.class));
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
