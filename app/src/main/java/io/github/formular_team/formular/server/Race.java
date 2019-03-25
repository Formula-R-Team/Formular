package io.github.formular_team.formular.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

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

    private final List<Racer> sortedRacers = Lists.newArrayList();

    private final float finishline;

    private final Node[] checkpoints;

    private float countdown = -1.0F;

    private boolean completed;

    private Race(final GameModel game, final RaceConfiguration configuration, final User owner, final Course course) {
        this.game = game;
        this.configuration = configuration;
        this.owner = owner;
        this.course = course;
        this.checkpoints = this.createCheckpointNodes(course.getTrack().getCheckpoints());
        this.finishline = this.checkpoints[0].cp.getPosition();
    }

    public void addListener(final RaceListener listener) {
        this.listeners.add(listener);
    }

    private Node[] createCheckpointNodes(final ImmutableList<Checkpoint> checkpoints) {
        final Node[] nodes = new Node[checkpoints.size()];
        final UnmodifiableIterator<Checkpoint> it = checkpoints.iterator();
        final Node first = new Node(it.next());
        int index = 0;
        nodes[index++] = first;
        Node last = first;
        while (it.hasNext()) {
            final Node cur = new Node(it.next());
            last = nodes[index++] = (cur.prev = last).next = cur;
        }
        (first.prev = last).next = first;
        Node nextRequired = first;
        for (Node cur = last; cur != first; cur = cur.prev) {
            cur.nextRequired = nextRequired;
            if (cur.cp.isRequired()) {
                nextRequired = cur;
            }
        }
        first.nextRequired = nextRequired;
        return nodes;
    }

    public void add(final Driver driver) {
        final Track.Pose pose = this.course.getTrack().getStartPlacement(this.racers.size());
        driver.getVehicle().setPosition(pose.position);
        driver.getVehicle().setRotation(-pose.rotation);
        final Racer racer = new Racer(driver);
        this.racers.add(racer);
        this.sortedRacers.add(racer);
        this.sort();
    }

    public void step(final float delta) {
        if (this.countdown > -1.0F) {
            final int c = (int) Mth.ceil(this.countdown);
            this.countdown -= delta;
            if ((int) Mth.ceil(this.countdown) != c) {
                this.onCount(c);
            }
            if (this.countdown < -1.0F) {
                this.countdown = -1.0F;
            }
        }
        for (final Racer racer : this.racers) {
            racer.step(delta);
        }
        this.sort();
        if (!this.completed && !this.sortedRacers.isEmpty() && this.sortedRacers.get(0).lap >= Race.this.configuration.getLapCount()) {
            Race.this.onEnd();
            this.completed = true;
        }
    }

    private void sort() {
        this.sortedRacers.sort(Comparator.<Racer, Float>comparing(r -> r.lap + r.progress).reversed());
        for (final ListIterator<Racer> it = this.sortedRacers.listIterator(); it.hasNext(); ) {
            final int p = it.nextIndex();
            final Racer r = it.next();
            if (r.position != p) {
                r.position = p;
                this.onPosition(r.driver, r.position);
            }
        }
    }

    private void onCount(final int count) {
        for (final RaceListener listener : this.listeners) {
            listener.onCount(count);
        }
    }

    private void onBegin() {
        for (final RaceListener listener : this.listeners) {
            listener.onBegin();
        }
        this.countdown = 3.0F;
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

    private void onPosition(final Driver driver, final int position) {
        for (final RaceListener listener : this.listeners) {
            listener.onPosition(driver, position);
        }
    }

    private void onLapComplete(final Driver driver, final int lap) {
        for (final RaceListener listener : this.listeners) {
            listener.onLap(driver, lap);
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

    public void begin() {
        this.onBegin();
    }

    private float getProgress(final float position) {
        return Mth.mod(position - this.finishline, 1.0F);
    }

    private class Racer {
        private final Driver driver;

        private Node lastNode;

        private float traveled = 0.0F, traveledRecord = 1.0F;

        private Node node;

        private int position;

        private int lap = 0;

        private float progress = -0.5F;

        private Racer(final Driver driver) {
            this.driver = driver;
            this.node = Race.this.checkpoints[0];
        }

        private void step(final float delta) {
            final Vector2 pos = this.driver.getVehicle().getPosition();
            final Node cp = this.test(pos);
            if (cp != null) {
                if (cp != this.lastNode) {
                    final float progress = cp.getProgress();
                    final float d = Mth.deltaMod(progress, this.node.getProgress(), 1.0F);
                    if (cp.cp.isRequired() && d > 0.0F && d < 0.5F) {
                        this.node = cp;
                        if (cp.cp.getIndex() == 0) {
                            this.lap++;
                            Race.this.onLapComplete(this.driver, this.lap);
                        }
                    }
                }
                this.travel(this.progress(cp, pos));
            }
            this.lastNode = cp;
        }

        private float progress(final Node cp, final Vector2 pos) {
            final Vector2 uv = new Vector2();
            if (this.ibilinear(pos, cp.cp.getP1(), cp.cp.getP2(), cp.next.cp.getP2(), cp.next.cp.getP1(), uv)) {
                final float p0 = cp.getPosition();
                final float p1 = cp.next.getPosition();
                final float posi = Mth.mod(p0 + Mth.deltaMod(p1, p0, 1.0F) * uv.getY(), 1.0F);
                final float prog = Race.this.getProgress(posi);
                final Node next = this.node.nextRequired;
                final float lastProgress = this.progress;
                if (next.cp.getIndex() != 0 && prog > next.getProgress()) {
                    this.progress = prog - 1.0F;
                } else {
                    this.progress = prog;
                }
                Race.this.onProgress(this.driver, this.progress);
                return Mth.deltaMod(this.progress, lastProgress, 1.0F);
            }
            return 0.0F;
        }

        private void travel(final float progression) {
            final float change = progression * Race.this.course.getTrack().getRoadPath().getLength();
            this.traveled = Mth.clamp(this.traveled + change, -4.0F, 4.0F);
            if (Math.abs(this.traveled) >= 4.0F && this.traveled * this.traveledRecord < 0.0F) {
                this.traveledRecord = this.traveled;
                if (this.traveled > 0.0F) {
                    Race.this.onForward(this.driver);
                } else {
                    Race.this.onReverse(this.driver);
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

        // TODO: more efficient testing with trees
        private Node test(final Vector2 p) {
            for (final Node cp : Race.this.checkpoints) {
                if (cp.contains(p)) {
                    return cp;
                }
            }
            return null;
        }
    }

    private final class Node {
        private final Checkpoint cp;

        private Node prev, next;

        private Node nextRequired;

        private Node(final Checkpoint cp) {
            this.cp = cp;
        }

        public boolean contains(final Vector2 point) {
            final float a = this.test(     this.cp.getP1(), point,      this.cp.getP2());
            final float b = this.test(     this.cp.getP2(), point, this.next.cp.getP2());
            final float c = this.test(this.next.cp.getP2(), point, this.next.cp.getP1());
            final float d = this.test(this.next.cp.getP1(), point,      this.cp.getP1());
            return a == c && (b == 0.0F || a == b) && (d == 0.0F || a == d);
        }

        private float test(final Vector2 i0, final Vector2 p, final Vector2 i1) {
            return Math.signum(i1.clone().sub(i0).cross(p.clone().sub(i0)));
        }

        public float getPosition() {
            return this.cp.getPosition();
        }

        private float getProgress() {
            return Race.this.getProgress(this.getPosition());
        }
    }
}
