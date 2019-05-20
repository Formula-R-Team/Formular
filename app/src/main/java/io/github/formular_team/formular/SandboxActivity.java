package io.github.formular_team.formular;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.OrientationEventListener;

import com.google.ar.core.Frame;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.concurrent.CompletableFuture;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import io.github.formular_team.formular.ar.Rectifier;
import io.github.formular_team.formular.core.User;

public class SandboxActivity extends FormularActivity implements ArActivity {
    private static final String TAG = "SandboxActivity";

    private User user;

    private ArFragment arFragment;

    private NavController controller;

    private KartNodeFactory factory;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sandbox);
        this.user = AppPreferences.getUser(PreferenceManager.getDefaultSharedPreferences(this));
        this.arFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        if (this.arFragment == null) {
            throw new IllegalStateException("ar fragment is null");
        }
        this.controller = Navigation.findNavController(this, R.id.ar_interface);
        final CompletableFuture<Texture> texture = Texture.builder()
            .setSampler(Texture.Sampler.builder()
                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
                .build())
            .setSource(this, R.drawable.blueprint).build();
        this.arFragment.getArSceneView().getPlaneRenderer().getMaterial().thenAcceptBoth(texture, (mat, tex) -> {
            mat.setTexture(PlaneRenderer.MATERIAL_TEXTURE, tex);
            mat.setFloat2(PlaneRenderer.MATERIAL_UV_SCALE, 4.0F, 4.0F);
        });
        final WeakOptional<SandboxActivity> act = WeakOptional.of(this);
        SimpleKartNodeFactory.create(this, R.raw.kart_body, R.raw.kart_wheel_front, R.raw.kart_wheel_rear)
            .thenAccept(factory -> act.ifPresent(activity -> activity.factory = factory));
        final OrientationEventListener listener = new OrientationEventListener(this) {
            private final int[] orientations = {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            };

            private int orientation = -1;

            @Override
            public void onOrientationChanged(final int orientation) {
                if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    final int o = this.orientations[(orientation + 360 - 45) / 90 % 4];
                    if (this.orientation != o) {
                        SandboxActivity.this.setRequestedOrientation(o);
                        this.orientation = o;
                    }
                }
            }
        };
        if (listener.canDetectOrientation()) {
            listener.enable();
        }
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public @Nullable Rectifier createRectifier() {
        final Frame frame = this.arFragment.getArSceneView().getArFrame();
        if (frame == null) {
            throw new IllegalStateException("frame is null");
        }
        try {
            return new Rectifier(frame);
        } catch (final NotYetAvailableException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Scene getScene() {
        return this.arFragment.getArSceneView().getScene();
    }

    @Override
    public void setOnTapArPlaneListener(final @Nullable BaseArFragment.OnTapArPlaneListener listener) {
        this.arFragment.setOnTapArPlaneListener(listener);
    }
}
