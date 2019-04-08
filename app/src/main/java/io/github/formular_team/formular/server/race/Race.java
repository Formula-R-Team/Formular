package io.github.formular_team.formular.server.race;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import io.github.formular_team.formular.server.GameModel;
import io.github.formular_team.formular.server.User;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.server.Checkpoint;
import io.github.formular_team.formular.server.Course;
import io.github.formular_team.formular.server.Driver;
import io.github.formular_team.formular.server.Track;

public final class Race {
    private final GameModel game;

    private final RaceConfiguration configuration;

    private final User owner;

    private final Course course;

    private final List<RaceListener> listeners = Lists.newArrayList();

    private final List<Racer> racers = Lists.newArrayList();

    private final List<Racer> sortedRacers = Lists.newArrayList();

    private final float finishline;

    private final float length;

    private final Node[] checkpoints;

    private RaceState state;

    private Race(final GameModel game, final RaceConfiguration configuration, final User owner, final Course course) {
        this.game = game;
        this.configuration = configuration;
        this.owner = owner;
        this.course = course;
        this.checkpoints = this.createCheckpointNodes(course.getTrack().getCheckpoints());
        this.finishline = this.checkpoints[0].cp.getPosition();
        this.length = course.getTrack().getRoadPath().getLength();
        this.state = new RaceState.Start(this);
    }

    public RaceConfiguration getConfiguration() {
        return this.configuration;
    }

    public float getLength() {
        return this.length;
    }

    public void addListener(final RaceListener listener) {
        this.listeners.add(listener);
    }

    public Racer getRacerByPlace(final int place) {
        return place < this.sortedRacers.size() ? this.sortedRacers.get(place) : null;
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
        final Racer racer = new Racer(this, driver);
        this.racers.add(racer);
        this.sortedRacers.add(racer);
        this.sortRacers();
        this.onLapComplete(driver, 0);
    }

    public void step(final float delta) {
        this.state = this.state.step(delta);
    }

    void stepRacers(final float delta) {
        for (final Racer racer : this.racers) {
            racer.step(delta);
        }
        this.sortRacers();
    }

    private void sortRacers() {
        this.sortedRacers.sort(Comparator.comparing(Racer::getLapProgress).reversed());
        for (final ListIterator<Racer> it = this.sortedRacers.listIterator(); it.hasNext(); ) {
            final int position = it.nextIndex();
            final Racer racer = it.next();
            racer.setPosition(position);
        }
    }

    public void start() {
        this.state = new RaceState.Starting(this);
        this.onBegin();
    }

    float getProgress(final float position) {
        return Mth.mod(position - this.finishline, 1.0F);
    }

    void onCount(final int count) {
        for (final RaceListener listener : this.listeners) {
            listener.onCount(count);
        }
    }

    void onBegin() {
        for (final RaceListener listener : this.listeners) {
            listener.onBegin();
        }
    }

    void onEnd() {
        for (final RaceListener listener : this.listeners) {
            listener.onEnd();
        }
    }

    void onProgress(final Driver driver, final float progress) {
        for (final RaceListener listener : this.listeners) {
            listener.onProgress(driver, progress);
        }
    }

    void onPosition(final Driver driver, final int position) {
        for (final RaceListener listener : this.listeners) {
            listener.onPosition(driver, position);
        }
    }

    void onLapComplete(final Driver driver, final int lap) {
        for (final RaceListener listener : this.listeners) {
            listener.onLap(driver, lap);
        }
    }

    void onForward(final Driver driver) {
        for (final RaceListener listener : this.listeners) {
            listener.onForward(driver);
        }
    }

    void onReverse(final Driver driver) {
        for (final RaceListener listener : this.listeners) {
            listener.onReverse(driver);
        }
    }

    // TODO: more efficient testing with trees
    public Node intersect(final Vector2 pos) {
        for (final Race.Node node : this.checkpoints) {
            if (node.contains(pos)) {
                return node;
            }
        }
        return null;
    }

    public Node getStartNode() {
        return this.checkpoints[0];
    }

    public static Race create(final GameModel game, final RaceConfiguration configuration, final User owner, final Course course) {
        return new Race(game, configuration, owner, course);
    }

    final class Node {
        final Checkpoint cp;

        Node prev, next;

        Node nextRequired;

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
    }
}
