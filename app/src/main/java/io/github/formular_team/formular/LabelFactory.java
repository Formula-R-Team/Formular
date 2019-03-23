package io.github.formular_team.formular;

import android.content.Context;
import android.widget.TextView;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.FixedHeightViewSizer;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

public final class LabelFactory {
    public static CompletableFuture<Node> create(final Context context, final String text, final float height) {
        return ViewRenderable.builder()
            .setView(context, R.layout.ar_labelplate)
            .build()
            .thenApply(renderable -> {
                renderable.setSizer(new FixedHeightViewSizer(height));
                renderable.setShadowCaster(false);
                renderable.setShadowReceiver(false);
                ((TextView) renderable.getView()).setText(text);
                final Node plate = new Node() {
                    @Override
                    public void onUpdate(final FrameTime frameTime) {
                        super.onUpdate(frameTime);
                        final Scene scene = this.getScene();
                        if (scene != null) {
                            final Quaternion q = scene.getCamera().getWorldRotation();
                            this.setWorldRotation(Quaternion.lookRotation(
                                Vector3.add(
                                    this.getWorldPosition(),
                                    Quaternion.rotateVector(q, Vector3.forward())
                                ),
                                Quaternion.rotateVector(q, Vector3.up())
                            ));
                        }
                    }
                };
                plate.setRenderable(renderable);
                return plate;
            });
    }

    public static Vector3 projectPoint(final Vector3 normal, final Vector3 point) {
        return Vector3.add(point, normal.scaled(-Vector3.dot(normal, point)));
    }
}
