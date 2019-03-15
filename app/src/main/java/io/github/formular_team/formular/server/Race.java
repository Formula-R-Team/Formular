package io.github.formular_team.formular.server;

import java.util.List;

import io.github.formular_team.formular.GameModel;
import io.github.formular_team.formular.math.Vector2;

public final class Race {
    private GameModel game;

    private final CheckPointNode[] quads = new CheckPointNode[0];

    private List<Racer> racers;

    private Course course;

    void step(final float delta) {
        for (final Racer racer : this.racers) {
            racer.step(delta);
        }
    }

    private void onBegin() {}

    private void onEnd() {}

    private void onProgress(final Driver driver) {}

    private void onLapComplete(final Driver driver) {}

    private void onForward(final Driver driver) {}

    private void onReverse(final Driver driver) {}

    private class Racer {
        private final Driver driver;

        private int index;

        private int position;

        private int lap;

        private float progress;

        private Racer(final Driver driver) {
            this.driver = driver;
        }

        void step(final float delta) {}
    }

    private CheckPointNode getCheckpoint(final int index) {
        return this.quads[Math.floorMod(index, this.quads.length)];
    }

    private class CheckPointNode {
        private final CheckPoint checkPoint;

        private final CheckPointNode next;

        CheckPointNode(final CheckPoint checkPoint, final CheckPointNode next) {
            this.checkPoint = checkPoint;
            this.next = next;
        }

        boolean contains(final Vector2 point) {
            final Vector2 p3 = this.next.checkPoint.getP2(), p4 = this.next.checkPoint.getP1();
            return this.test(point, this.checkPoint.getP1(), this.checkPoint.getP2()) == this.test(point, this.checkPoint.getP2(), p3) ==
                this.test(point, p3, p4) == this.test(point, p4, this.checkPoint.getP1());
        }

        private boolean test(final Vector2 p, final Vector2 i0, final Vector2 i1) {
            final Vector2 edge = i1.clone();
            edge.sub(i0);
            final Vector2 beta = p.clone();
            beta.sub(i0);
            return edge.cross(beta) > 0.0F;
        }
    }
}
