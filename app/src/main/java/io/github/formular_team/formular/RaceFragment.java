package io.github.formular_team.formular;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class RaceFragment extends Fragment implements RaceView {
    private View view;

    private ArActivity activity;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            setViewLayout(R.layout.fragment_race_land, getLap(), getPosition());
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setViewLayout(R.layout.fragment_race, getLap(), getPosition());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_race, container, false);
        this.view.<SteeringWheelView>findViewById(R.id.steering_wheel).setSteerListener(state -> this.activity.onSteer(state));
        final WifiManager wifi = this.requireContext().getSystemService(WifiManager.class);
        final String ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        final TextView ipText = this.view.findViewById(R.id.ip);
        ipText.setText(ip);
        ipText.setOnClickListener(v -> {
            v.setVisibility(View.INVISIBLE);
            this.activity.startRace();
        });
        return this.view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof ArActivity) {
            this.activity = (ArActivity) context;
        } else {
            throw new RuntimeException(context + " must implement ArActivity");
        }
        this.activity.addRaceListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
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
        posText.setText(this.getString(resID, position));
    }

    public String getPosition() {
        final TextView posText = this.view.findViewById(R.id.position);
        return (String) posText.getText();
    }

    @Override
    public void setLap(final @StringRes int resID, final int lap, final int lapCount) {
        final TextView lapText = this.view.findViewById(R.id.lap);
        lapText.setText(this.getString(resID, lap, lapCount));
    }

    public String getLap(){
        final TextView lapText = this.view.findViewById(R.id.lap);
        return (String) lapText.getText();
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

    private void setViewLayout(int id, String lap, String position){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(id, null);
        ViewGroup rootView = (ViewGroup) this.getView();
        rootView.removeAllViews();
        rootView.addView(view);

        this.view.<SteeringWheelView>findViewById(R.id.steering_wheel).setSteerListener(state -> this.activity.onSteer(state));

        final TextView lapText = this.view.findViewById(R.id.lap);
        lapText.setVisibility(View.VISIBLE);
        lapText.setText(lap);

        final TextView positionText = this.view.findViewById(R.id.position);
        positionText.setVisibility(View.VISIBLE);
        positionText.setText(position);
    }
}
