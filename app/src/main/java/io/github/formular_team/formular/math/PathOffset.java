package io.github.formular_team.formular.math;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import io.github.formular_team.formular.collision.Intersections;

public final class PathOffset {
    public static final class Frame {
        private final float t;

        private final Vector2 p1;

        private final Vector2 p2;

        private Frame next, prev;

        private Frame(final float t, final Vector2 p1, final Vector2 p2) {
            this.t = t;
            this.p1 = p1;
            this.p2 = p2;
        }

        public float getT() {
            return this.t;
        }

        public Vector2 getP1() {
            return this.p1;
        }

        public Vector2 getP2() {
            return this.p2;
        }
    }

    public static List<Frame> createFrames(final Curve path, final float start, final int steps, final float width) {
        final List<Frame> frames = Lists.newArrayListWithExpectedSize(steps);
        final Frame head = frame(path, start, width);
        frames.add(head);
        Frame tail = head;
        for (int n = 1; n < steps; n++) {
            final Frame cur = frame(path, start + n / (float) steps, width);
            cur.prev = tail;
            tail.next = cur;
            frames.add(cur);
            tail = cur;
        }
        (head.prev = tail).next = head;
        clip(frames, steps, f -> f.p1);
        clip(frames, steps, f -> f.p2);
        cull(frames);
        return frames;
    }

    private static Frame frame(final Curve path, final float t, final float width) {
        final Vector2 point = path.getPoint(t);
        final Vector2 normal = path.getTangent(t).rotate();
        final Vector2 v0 = normal.clone().multiply(-0.5F * width).add(point);
        final Vector2 v1 = normal.clone().multiply(0.5F * width).add(point);
        return new Frame(t, v0, v1);
    }

    private static void clip(final List<Frame> frames, final int steps, final Function<Frame, Vector2> p) {
        outer:
        for (int n = 0; n <= steps; ) {
            final Frame f0 = frames.get(n % frames.size());
            // TODO: adaptive lead
            for (int lead = 2; lead <= 12; lead++) {
                final int ln = n + lead;
                final Frame f1 = frames.get(ln % frames.size());
                final Vector2 r = new Vector2();
                if (Intersections.lineLine(p.apply(f0), p.apply(f0.next), p.apply(f1), p.apply(f1.next), r)) {
                    while (++n <= ln) {
                        p.apply(frames.get(n % frames.size())).copy(r);
                    }
                    n--;
                    continue outer;
                }
            }
            n++;
        }
    }

    private static void cull(final List<Frame> frames) {
        for (final Iterator<Frame> it = frames.iterator(); it.hasNext(); ) {
            final Frame f = it.next();
            if (f.p1.distanceTo(f.next.p1) < 1.0e-3F && f.p2.distanceTo(f.next.p2) < 1.0e-3F) {
                f.prev.next = f.next;
                f.next.prev = f.prev;
                it.remove();
            }
        }
    }
}
