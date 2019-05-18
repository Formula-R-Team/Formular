package io.github.formular_team.formular;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import io.github.formular_team.formular.core.kart.KartModel;

public class ArInterfaceFragment extends Fragment implements ArInterfaceView {
    private View view;

    private ArInterfaceListener listener;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_ar_interface, container, false);
        this.view.<SteeringWheelView>findViewById(R.id.steering_wheel).setSteerListener(this::onSteer);
        return this.view;
    }

    public void onSteer(final KartModel.ControlState state) {
        if (this.listener != null) {
            this.listener.onSteer(state);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof ArInterfaceListener) {
            this.listener = (ArInterfaceListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void setCount(final int resID) {
        final TextView countText = this.view.findViewById(R.id.count);
        countText.setText(resID);
        final Animation anim = new AlphaAnimation(1.0F, 0.0F);
        anim.setDuration(1000);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        countText.startAnimation(anim);
    }

    @Override
    public void setPosition(final int resID, final int position) {
        final TextView posText = this.view.findViewById(R.id.position);
        posText.setText(resID);
    }

    @Override
    public void setLap(@StringRes final int resID, final int lap, final int lapCount) {
        final TextView lapText = this.view.findViewById(R.id.lap);
        lapText.setText(resID);
    }

    @Override
    public void setFinish() {
        final TextView countText = this.view.findViewById(R.id.count);
        countText.setText(R.string.race_finish);
        final Animation anim = new AlphaAnimation(1.0F, 0.0F);
        anim.setStartOffset(1200);
        anim.setDuration(1000);
        anim.setFillEnabled(true);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        countText.startAnimation(anim);
    }

    public static ArInterfaceFragment newInstance() {
        final ArInterfaceFragment fragment = new ArInterfaceFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public interface ArInterfaceListener {
        void onSteer(final KartModel.ControlState state);
    }
}
