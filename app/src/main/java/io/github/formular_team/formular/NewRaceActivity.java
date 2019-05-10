package io.github.formular_team.formular;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import io.github.formular_team.formular.core.race.RaceConfiguration;


public class NewRaceActivity extends FormularActivity implements AdapterView.OnItemSelectedListener{
    String[] raceOptions = new String[]{"Time Trial","Versus"};
    String[] racerCapOptions = new String[]{"2","3","4"};

    MyViewModel myViewModel;
    RaceConfiguration raceConfiguration;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        this.setContentView(R.layout.activity_new_race);
        this.setupButtons();

        final Spinner gameType = this.findViewById(R.id.game_type_spinner);
        gameType.setOnItemSelectedListener(this);

        final ArrayAdapter<String> aa = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, this.raceOptions);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        gameType.setAdapter(aa);

        final Spinner racerCap = this.findViewById(R.id.racer_cap);
        gameType.setOnItemSelectedListener(this);

        final ArrayAdapter<String> aa2 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, this.racerCapOptions);
        aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        racerCap.setAdapter(aa2);
    }

    private void setupButtons() {
        final Button btnContinue = this.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            this.myViewModel.setRaceConfiguration(this.raceConfiguration);
            this.startActivity(new Intent(this, SandboxActivity.class));
        });
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {

    }
}