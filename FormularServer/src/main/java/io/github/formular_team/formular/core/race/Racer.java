package io.github.formular_team.formular.core.race;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Vector2;

class Racer {
    private final Race race;

    private final Driver driver;

    private Race.Node lastNode;

    private float traveled = 0.0F, traveledRecord = 1.0F;

    private Race.Node node;

    private int position = -1;

    private int lap = 0;

    private float progress = -0.5F;

    Racer(final Race race, final Driver driver) {
        this.race = race;
        this.driver = driver;
        this.node = this.race.getStartNode();
    }

    float getLapProgress() {
        return this.lap + this.progress;
    }

    void setPosition(final int position) {
        if (this.position != position) {
            this.position = position;
            this.race.onPosition(this.driver, position);
        }
    }

    void step(final float delta) {
        final Vector2 pos = this.driver.getVehicle().getPosition();
        final Race.Node node = this.race.intersect(pos);
        if (node != null) {
            this.travel(this.progress(node, pos));
            if (node != this.lastNode) {
                final float d = Mth.deltaMod(node.getPosition(), this.node.getPosition(), 1.0F);
                if (node.cp.isRequired() && d > 0.0F && d < 0.5F) {
                    this.node = node;
                    if (node.cp.getIndex() == 0) {
                        this.lap++;
                        this.race.onLapComplete(this.driver, this.lap);
                    }
                }
            }
        }
        this.lastNode = node;
    }

    private float progress(final Race.Node cp, final Vector2 pos) {
        final Vector2 uv = new Vector2();
        if (this.ibilinear(pos, cp.cp.getP1(), cp.cp.getP2(), cp.next.cp.getP2(), cp.next.cp.getP1(), uv)) {
            final float p0 = cp.getPosition();
            final float p1 = cp.next.getPosition();
            final float posi = Mth.mod(p0 + Mth.deltaMod(p1, p0, 1.0F) * uv.getY(), 1.0F);
            final float prog = this.race.getProgress(posi);
            final Race.Node next = this.node.nextRequired;
            final float lastProgress = this.progress;
            if (next.cp.getIndex() != 0 && prog > this.race.getProgress(next.getPosition())) {
                this.progress = prog - 1.0F;
            } else {
                this.progress = prog;
            }
            this.race.onProgress(this.driver, this.progress);
            return Mth.deltaMod(this.progress, lastProgress, 1.0F);
        }
        return 0.0F;
    }

    private void travel(final float progression) {
        final float change = progression * this.race.getLength();
        this.traveled = Mth.clamp(this.traveled + change, -4.0F, 4.0F);
        if (Math.abs(this.traveled) >= 4.0F && this.traveled * this.traveledRecord < 0.0F) {
            this.traveledRecord = this.traveled;
            if (this.traveled > 0.0F) {
                this.race.onForward(this.driver);
            } else {
                this.race.onReverse(this.driver);
            }
        }
    }

    // https://iquilezles.org/www/articles/ibilinear/ibilinear.htm
    private boolean ibilinear(final Vector2 p, final Vector2 a, final Vector2 b, Vector2 c, Vector2 d, final Vector2 result) {
        // TODO: better triangle case
        if (a.equals(d)) {
            d = c.clone().sub(b).setLength(1e-3F).add(d);
        } else if (b.equals(c)) {
            c = d.clone().sub(a).setLength(1e-3F).add(c);
        }
        final Vector2 e = b.clone().sub(a);
        final Vector2 f = d.clone().sub(a);
        final Vector2 g = a.clone().sub(b).add(c).sub(d);
        final Vector2 h = p.clone().sub(a);
        final float k2 = g.cross(f);
        final float k1 = e.cross(f) + h.cross(g);
        final float k0 = h.cross(e);
        if (Math.abs(k2) < 1e-3F) {
            final float v = -k0 / k1;
            if (v >= 0.0F && v <= 1.0F) {
                final float u  = (h.getX() * k1 + f.getX() * k0) / (e.getX() * k1 - g.getX() * k0);
                if (u >= 0.0F && u < 1.0F) {
                    result.set(u, v);
                    return true;
                }
            }
            return false;
        }
        float w = k1 * k1 - 4.0F * k0 * k2;
        if (w >= 0.0F) {
            w = Mth.sqrt(w);
            final float v1 = (-k1 - w) / (2.0F * k2);
            final float v = v1 >= 0.0F && v1 <= 1.0F ? v1 : (-k1 + w) / (2.0F * k2);
            if (v >= 0.0F && v <= 1.0F) {
                final float u = (h.getX() - f.getX() * v) / (e.getX() + g.getX() * v);
                if (u >= 0.0F && u <= 1.0F) {
                    result.set(u, v);
                    return true;
                }
            }
        }
        return false;
    }
}
