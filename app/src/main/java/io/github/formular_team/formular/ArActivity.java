package io.github.formular_team.formular;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import io.github.formular_team.formular.viewmodels.ArViewModel;

public final class ArActivity extends FormularActivity {
    private ArViewModel viewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_ar);
        this.viewModel = ViewModelProviders.of(this).get(ArViewModel.class);
//        final ArFragment ar = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
//        if (ar != null) {
//            final Scene scene = ar.getArSceneView().getScene();
//        }
    }
}
