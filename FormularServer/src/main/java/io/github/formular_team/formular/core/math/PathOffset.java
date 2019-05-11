package io.github.formular_team.formular.core.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.formular_team.formular.core.math.curve.Curve;

public final class PathOffset {
    public static final class Frame {
        private final float t;

        private final Vector2 p1;

        private final Vector2 p2;

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

    public static List<Frame> offset(final List<Vector2> points, final float width) {
        final List<Frame> frames = new ArrayList<>(points.size());
        Frame head = null;
        for (int i = 0; i < points.size(); i++) {
            final Vector2 pBefore = points.get((i + points.size() - 1) % points.size());
            final Vector2 pNow = points.get(i);
            final Vector2 pAfter = points.get((i + 1) % points.size());
            final Vector2 nowToBefore = pNow.clone().sub(pBefore);
            final Vector2 afterToNow = pAfter.clone().sub(pNow);
            final float t = nowToBefore.length() / (nowToBefore.length() + afterToNow.length());
            final Vector2 in = nowToBefore.setLength(1.0F - t);
            final Vector2 out = afterToNow.setLength(t);
            final Vector2 tangent = in.clone().add(out).normalize();
            final Vector2 normal = tangent.rotate();
            final Vector2 v0 = normal.clone().multiply(-0.5F * width).add(pNow);
            final Vector2 v1 = normal.clone().multiply(0.5F * width).add(pNow);
            final Frame cur = new Frame(0.0F, v0, v1);
            if (head == null) {
                head = cur;
            }
            frames.add(cur);
        }
        clip(frames, f -> f.p1);
        clip(frames, f -> f.p2);
        cull(frames);
        return frames;
    }

    public static List<Frame> createFrames(final Curve path, final float start, final int steps, final float width) {
        final List<Frame> frames = new ArrayList<>(steps);
        final Frame head = frame(path, start, width);
        frames.add(head);
        for (int n = 1; n < steps; n++) {
            final Frame cur = frame(path, start + n / (float) steps, width);
            frames.add(cur);
        }
        clip(frames, f -> f.p1);
        clip(frames, f -> f.p2);
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

    private static void clip(final List<Frame> frames, final Function<Frame, Vector2> p) {
        outer:
        for (int n = 0; n <= frames.size(); ) {
            final Frame f0 = frames.get(n % frames.size());
            final Frame f0next = frames.get((n + 1) % frames.size());
            // TODO: adaptive lead
            for (int lead = 2; lead <= 24; lead++) {
                final int ln = n + lead;
                final Frame f1 = frames.get(ln % frames.size());
                final Frame f1next = frames.get((ln + 1) % frames.size());
                final Vector2 r = new Vector2();
                if (Intersections.lineLine(p.apply(f0), p.apply(f0next), p.apply(f1), p.apply(f1next), r)) {
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
        for (int i = 0; i < frames.size(); ) {
            final Frame f = frames.get(i);
            final Frame fnext = frames.get((i + 1) % frames.size());
            if (f.p1.distanceTo(fnext.p1) < 1.0e-3F && f.p2.distanceTo(fnext.p2) < 1.0e-3F) {
                frames.remove(i);
            } else {
                i++;
            }
        }
    }
}
