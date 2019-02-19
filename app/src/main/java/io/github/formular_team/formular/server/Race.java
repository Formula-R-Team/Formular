package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Vector2;

public final class Race {
    private final CheckPointQuad[] quads = new CheckPointQuad[0];

    void step(final float delta) {

    }

    private class Racer {
        private final Driver driver;

        private Racer(final Driver driver) {
            this.driver = driver;
        }
    }

    private CheckPointQuad getCheckpoint(final int index) {
        return this.quads[Math.floorMod(index, this.quads.length)];
    }

    private class CheckPointQuad {
        private final Vector2 p1;

        private final Vector2 p2;

        private final int index;

        private final CheckPointQuad next;

        CheckPointQuad(final Vector2 p1, final Vector2 p2, final int index, final CheckPointQuad next) {
            this.p1 = p1;
            this.p2 = p2;
            this.index = index;
            this.next = next;
        }

        boolean contains(final Vector2 point) {
            final Vector2 p3 = this.next.p2, p4 = this.next.p1;
            return this.test(point, this.p1, this.p2) == this.test(point, this.p2, p3) ==
                this.test(point, p3, p4) == this.test(point, p4, this.p1);
        }

        private boolean test(final Vector2 p, final Vector2 i0, final Vector2 i1) {
            final Vector2 edge = i1.copy();
            edge.sub(i0);
            final Vector2 beta = p.copy();
            beta.sub(i0);
            return edge.cross(beta) > 0.0F;
        }
    }
}
