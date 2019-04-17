package io.github.formular_team.formular;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
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
    private Button btnContinue;
    MyViewModel myViewModel;
    RaceConfiguration raceConfiguration;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        setContentView(R.layout.activity_new_race);
        this.setupButtons();

        Spinner gameType = (Spinner) findViewById(R.id.game_type_spinner);
        gameType.setOnItemSelectedListener(this);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,raceOptions);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        gameType.setAdapter(aa);

        Spinner racerCap = (Spinner) findViewById(R.id.racer_cap);
        gameType.setOnItemSelectedListener(this);

        ArrayAdapter aa2 = new ArrayAdapter(this,android.R.layout.simple_spinner_item,racerCapOptions);
        aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        racerCap.setAdapter(aa2);
    }

    private void setupButtons() {
        this.btnContinue = this.findViewById(R.id.btnContinue);
        this.btnContinue.setOnClickListener(this.activityChange(RaceActivity.class));
    }

    private View.OnClickListener activityChange(final Class<?> cls) {

        myViewModel.setRaceConfiguration(raceConfiguration);
        return v -> this.startActivity(new Intent(this, cls));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}