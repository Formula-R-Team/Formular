package io.github.formular_team.formular.ar;

import android.util.SparseArray;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;

import java.util.Optional;

import io.github.formular_team.formular.KartNodeFactory;
import io.github.formular_team.formular.core.GameView;
import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.KartDefinition;
import io.github.formular_team.formular.core.KartView;
import io.github.formular_team.formular.core.RacerStatus;
import io.github.formular_team.formular.core.StateKartView;
import io.github.formular_team.formular.core.math.Vector2;

public class ArGameView implements GameView {
    private final Scene scene;

    private final Node surface;

    private final KartNodeFactory factory;

    private final SparseArray<KartView> karts;

    private ArGameView(final Scene scene, final Node surface, final KartNodeFactory factory, final SparseArray<KartView> karts) {
        this.scene = scene;
        this.surface = surface;
        this.factory = factory;
        this.karts = karts;
    }

    @Override
    public RacerStatus getStatus() {
        return new RacerStatus();
    }

    @Override
    public Kart createKart(final int uniqueId) {
        final KartView kart = new StateKartView(uniqueId, KartDefinition.createKart2(), new Vector2(), 0.0F);
        this.surface.addChild(this.factory.create(kart));
        this.karts.put(uniqueId, kart);
        return kart;
    }

    @Override
    public Optional<Kart> removeKart(final int uniqueId) {
        final Kart kart = this.karts.get(uniqueId);
        if (kart == null) {
            return Optional.empty();
        }
        this.karts.remove(uniqueId);
        return Optional.of(kart);
    }

    @Override
    public Optional<Kart> getKart(final int uniqueId) {
        return Optional.ofNullable(this.karts.get(uniqueId));
    }

    public static ArGameView create(final Scene scene, final Node surface, final KartNodeFactory factory) {
        return new ArGameView(scene, surface, factory, new SparseArray<>());
    }
}
