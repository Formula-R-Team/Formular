package io.github.formular_team.formular;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class NewRaceActivity extends FormularActivity {
    private Button btnContinue;
    //data binding
    ViewDataBinding mBinding;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyViewModel myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_new_race);


        this.setupButtons();
    }

    private void setupButtons() {
        this.btnContinue = this.findViewById(R.id.btnContinue);
        this.btnContinue.setOnClickListener(this.activityChange(RaceActivity.class));
    }

    private View.OnClickListener activityChange(final Class<?> cls) {
        return v -> this.startActivity(new Intent(this, cls));
    }
}