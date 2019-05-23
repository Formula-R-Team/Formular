package io.github.formular_team.formular;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;

import org.hsluv.HUSLColorConverter;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.DirectKartView;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.kart.KartDefinition;
import io.github.formular_team.formular.core.kart.KartModel;
import io.github.formular_team.formular.core.math.Mth;

public class CustomizeFragment extends Fragment {
    private ArActivity activity;

    private HueSlider slider;

    private KartNode kart;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_customize, container, false);
        this.slider = view.findViewById(R.id.hue);
        final Context context = this.getContext();
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final io.github.formular_team.formular.core.color.Color userColor = AppPreferences.getUserColor(settings);
        this.slider.setProgress((int) (HUSLColorConverter.rgbToHsluv(new double[] { userColor.getRed(), userColor.getGreen(), userColor.getBlue() })[0] / 360.0F * 100.0F));
        this.slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int getArgb(final SeekBar seekBar) {
                final double[] c = HUSLColorConverter.hsluvToRgb(new double[] { seekBar.getProgress() / 100.0F * 360.0F, 85.0F, 30.0F });
                return 0xFF000000 |
                    Mth.clamp((int) (c[0] * 0xFF), 0x00, 0xFF) << 16 |
                    Mth.clamp((int) (c[1] * 0xFF), 0x00, 0xFF) << 8 |
                    Mth.clamp((int) (c[2] * 0xFF), 0x00, 0xFF);
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (CustomizeFragment.this.kart != null) {
                    CustomizeFragment.this.kart.setColor(new Color(this.getArgb(seekBar)));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                settings.edit().putInt("user.color", this.getArgb(seekBar)).apply(); // FIXME: AppPreferences
            }
        });
        final WeakOptional<ArActivity> act = WeakOptional.of(this.activity);
        final WeakOptional<CustomizeFragment> frag = WeakOptional.of(this);
        ModelRenderable.builder()
            .setSource(context, R.raw.shop_room)
            .build()
            .thenApply(r -> {
                final Node node = new Node();
//                node.setRenderable(r);
                return node;
            })
            .thenCombine(
                ModelRenderable.builder()
                    .setSource(context, R.raw.shop_platter)
                    .build()
                    .thenApply(r -> {
                        final Node node = new Node() {
                            float theta;

                            @Override
                            public void onUpdate(final FrameTime frameTime) {
                                super.onUpdate(frameTime);
                                this.setLocalRotation(Quaternion.axisAngle(Vector3.up(), this.theta));
                                this.theta -= frameTime.getDeltaSeconds() * 10.0F;
                            }
                        };
                        node.setRenderable(r);
                        return node;
                    }),
                (room, platter) -> act.map(activity -> {
                    final Node root = activity.getAnchor();
                    room.addChild(platter);
                    root.addChild(room);
                    final Node platterFloor = new Node();
                    platterFloor.setLocalPosition(new Vector3(0.0F, KartDefinition.inchToMeter(5.0F), -0.15F/*specific to kart model*/));
                    platter.addChild(platterFloor);
                    return platterFloor;
                }).orElse(null))
            .thenCombine(SimpleKartNodeFactory.create(context, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear), (platter, factory) -> {
                frag.ifPresent(f -> {
                    final KartNode kart = factory.create(new DirectKartView(new KartModel(0, KartDefinition.createKart2(), new SimpleControlState())));
                    kart.setColor(new Color(userColor.getHex()));
                    f.kart = kart;
                    platter.addChild(kart);
                });
                return null;
            }).exceptionally(t -> {
                act.ifPresent(activity -> {
                    new AlertDialog.Builder(context)
                        .setMessage(t.getMessage())
                        .setTitle("Error")
                        .create()
                        .show();
            });
            return null;
        });
        return view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof ArActivity) {
            this.activity = (ArActivity) context;
        } else {
            throw new RuntimeException(context + " must implement ArActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }
}
