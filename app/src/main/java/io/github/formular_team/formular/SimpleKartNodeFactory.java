package io.github.formular_team.formular;

import android.content.Context;
import android.support.annotation.RawRes;

import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.github.formular_team.formular.ar.KartNode;
import io.github.formular_team.formular.core.kart.KartView;

public class SimpleKartNodeFactory implements KartNodeFactory {
    private final ModelRenderable body;

    private final ModelRenderable wheelFront;

    private final ModelRenderable wheelRear;

    public SimpleKartNodeFactory(final ModelRenderable body, final ModelRenderable wheelFront, final ModelRenderable wheelRear) {
        this.body = body;
        this.wheelFront = wheelFront;
        this.wheelRear = wheelRear;
    }

    @Override
    public KartNode create(final KartView kart) {
        final ModelRenderable body = this.body.makeCopy();
        final ModelRenderable wheelFront = this.wheelFront.makeCopy();
        final ModelRenderable wheelRear = this.wheelRear.makeCopy();
        wheelFront.setMaterial(body.getMaterial());
        wheelRear.setMaterial(body.getMaterial());
        return KartNode.create(kart, body, wheelFront, wheelRear);
    }

    public static CompletableFuture<SimpleKartNodeFactory> create(final Context context, @RawRes final int bodyRes, @RawRes final int wheelFrontRes, @RawRes final int wheelRearRes) {
        final CompletableFuture<ModelRenderable> bodyFuture = ModelRenderable.builder()
            .setSource(context, bodyRes)
            .build();
        final CompletableFuture<ModelRenderable> wheelFrontFuture = ModelRenderable.builder()
            .setSource(context, wheelFrontRes)
            .build();
        final CompletableFuture<ModelRenderable> wheelRearFuture = ModelRenderable.builder()
            .setSource(context, wheelRearRes)
            .build();
        return CompletableFuture.allOf(bodyFuture, wheelFrontFuture, wheelRearFuture)
            .thenApply(v -> {
                final ModelRenderable body;
                final ModelRenderable wheelFront;
                final ModelRenderable wheelRear;
                try {
                    body = bodyFuture.get();
                    wheelFront = wheelFrontFuture.get();
                    wheelRear = wheelRearFuture.get();
                } catch (final ExecutionException | InterruptedException e) {
                    throw new AssertionError(e);
                }
                return new SimpleKartNodeFactory(
                    body,
                    wheelFront,
                    wheelRear
                );
            });
    }
}
