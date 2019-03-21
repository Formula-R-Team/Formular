package io.github.formular_team.formular.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.util.List;

import io.github.formular_team.formular.GameModel;
import io.github.formular_team.formular.User;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;

public final class Race {
    private final GameModel game;

    private final RaceConfiguration configuration;

    private final User owner;

    private final Course course;

    private final List<RaceListener> listeners = Lists.newArrayList();

    private final List<Racer> racers = Lists.newArrayList();

    private final CheckPointNode[] checkpoints;

    private final State state = State.READY;

    private Race(final GameModel game, final RaceConfiguration configuration, final User owner, final Course course) {
        this.game = game;
        this.configuration = configuration;
        this.owner = owner;
        this.course = course;
        this.checkpoints = this.createCheckpointNodes(course.getTrack().getCheckpoints());
    }

    public void addListener(final RaceListener listener) {
        this.listeners.add(listener);
    }

    private CheckPointNode[] createCheckpointNodes(final ImmutableList<Checkpoint> checkpoints) {
        final CheckPointNode[] nodes = new CheckPointNode[checkpoints.size()];
        final UnmodifiableIterator<Checkpoint> it = checkpoints.iterator();
        final CheckPointNode first = new CheckPointNode(it.next());
        int index = 0;
        nodes[index++] = first;
        CheckPointNode last = first;
        while (it.hasNext()) {
            final CheckPointNode cur = new CheckPointNode(it.next());
            last = nodes[index++] = (cur.prev = last).next = cur;
        }
        (first.prev = last).next = first;
        return nodes;
    }

    public void add(final Driver driver) {
        final Track.Pose pose = this.course.getTrack().getStartPlacement(this.racers.size());
        driver.getVehicle().setPosition(pose.position);
        driver.getVehicle().setRotation(-pose.rotation);
        this.racers.add(new Racer(driver));
        this.onLapComplete(driver, 0);
    }

    public void step(final float delta) {
        for (final Racer racer : this.racers) {
            racer.step(delta);
        }
    }

    private void onBegin() {
        for (final RaceListener listener : this.listeners) {
            listener.onBegin();
        }
    }

    private void onEnd() {
        for (final RaceListener listener : this.listeners) {
            listener.onEnd();
        }
    }

    private void onProgress(final Driver driver, final float progress) {
        for (final RaceListener listener : this.listeners) {
            listener.onProgress(driver, progress);
        }
    }

    private void onLapComplete(final Driver driver, final int lap) {
        for (final RaceListener listener : this.listeners) {
            listener.onLapComplete(driver, lap);
        }
    }

    private void onForward(final Driver driver) {
        for (final RaceListener listener : this.listeners) {
            listener.onForward(driver);
        }
    }

    private void onReverse(final Driver driver) {
        for (final RaceListener listener : this.listeners) {
            listener.onReverse(driver);
        }
    }

    public static Race create(final GameModel game, final RaceConfiguration configuration, final User owner, final Course course) {
        return new Race(game, configuration, owner, course);
    }

    private class Racer {
        private final Driver driver;

        private final int[] history = { -4, -3, -2, -1 };

        private int historyEnd;

        private int index;

        private int position;

        private int lap = 0;

        private float check;

        private float progress;

        private int travel = 1;

        private Racer(final Driver driver) {
            this.driver = driver;
        }

        private void step(final float delta) {
            final Vector2 p = this.driver.getVehicle().getPosition();
            final CheckPointNode cp = this.test(p);
            if (cp != null) {
                final int i = cp.point.getIndex();
                if (i != this.history[Math.floorMod(this.historyEnd - 1, this.history.length)]) {
                    final float progress = cp.point.getPosition();
                    final float d = Mth.deltaMod(progress, this.check, 1.0F);
                    if (cp.point.isRequired() && d > 0.0F && d < 0.5F) {
                        this.index = i;
                        this.check = progress;
                        if (this.index == 0) {
                            this.lap++;
                            Race.this.onLapComplete(this.driver, this.lap);
                        }
                    }
                    this.history[this.historyEnd] = i;
                    this.travel();
                    this.historyEnd = Math.floorMod(this.historyEnd + 1, this.history.length);
                }
                this.progress(cp, p);
            }
        }

        private void travel() {
            for (int i = 0; i < 1; i++) {
                final int d = Integer.signum(Mth.deltaMod(
                    this.history[Math.floorMod(this.historyEnd + i, this.history.length)],
                    this.history[Math.floorMod(this.historyEnd - 1 + i, this.history.length)],
                    Race.this.checkpoints.length
                ));
                if (this.travel == d) {
                    return;
                }
            }
            this.travel = -this.travel;
            if (this.travel > 0) {
                Race.this.onForward(this.driver);
            } else {
                Race.this.onReverse(this.driver);
            }
        }

        private void progress(final CheckPointNode cp, final Vector2 p) {
            final Vector2 uv = this.invBilinear(p, cp.point.getP1(), cp.point.getP2(), cp.next.point.getP2(), cp.next.point.getP1());
            if (uv.getY() != -1.0F) {
                final float p0 = cp.point.getPosition() - Race.this.checkpoints[0].point.getPosition();
                final float p1 = cp.next.point.getPosition() - Race.this.checkpoints[0].point.getPosition();
                this.progress = Mth.mod(p0 + Mth.deltaMod(p1, p0, 1.0F) * uv.getX(), 1.0F);
                Race.this.onProgress(this.driver, this.progress);
            }
        }

        // https://iquilezles.org/www/articles/ibilinear/ibilinear.htm
        private Vector2 invBilinear(final Vector2 p, final Vector2 a, final Vector2 b, Vector2 c, Vector2 d) {
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
            float w = k1 * k1 - 4.0F * k0 * k2;
            if (w < 0.0F) {
                return new Vector2(-1.0F, -1.0F);
            }
            w = Mth.sqrt(w);
            final float v1 = (-k1 - w) / (2.0F * k2);
            final float u1 = (h.getX() - f.getX() * v1) / (e.getX() + g.getX() * v1);
            final float v2 = (-k1 + w) / (2.0F * k2);
            final float u2 = (h.getX() - f.getX() * v2) / (e.getX() + g.getX() * v2);
            float u = u1;
            float v = v1;
            if (v < 0.0F || v > 1.0F || u < 0.0F || u > 1.0F) {
                u = u2;
                v = v2;
            }
            if (v < 0.0F || v > 1.0F || u < 0.0F || u > 1.0F) {
                u = -1.0F;
                v = -1.0F;
            }
            return new Vector2(u, v);
        }


            // TODO: more efficient testing with trees
        private CheckPointNode test(final Vector2 p) {
            for (final CheckPointNode cp : Race.this.checkpoints) {
                if (cp.contains(p)) {
                    return cp;
                }
            }
            return null;
        }
    }

    private final class CheckPointNode {
        private final Checkpoint point;

        private CheckPointNode prev, next;

        private CheckPointNode(final Checkpoint point) {
            this.point = point;
        }

        public boolean contains(final Vector2 point) {
            final float a = this.test(     this.point.getP1(), point,      this.point.getP2());
            final float b = this.test(     this.point.getP2(), point, this.next.point.getP2());
            final float c = this.test(this.next.point.getP2(), point, this.next.point.getP1());
            final float d = this.test(this.next.point.getP1(), point,      this.point.getP1());
            return a == c && (b == 0.0F || a == b) && (d == 0.0F || a == d);
        }

        private float test(final Vector2 i0, final Vector2 p, final Vector2 i1) {
            return Math.signum(i1.clone().sub(i0).cross(p.clone().sub(i0)));
        }
    }

    private enum State {
        READY,
        RACING,
        FINISHED
    }
}
