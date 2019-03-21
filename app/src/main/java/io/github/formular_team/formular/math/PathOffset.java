package io.github.formular_team.formular.math;

import com.google.common.collect.Lists;

import java.util.List;

import io.github.formular_team.formular.collision.Intersections;

public final class PathOffset {
    public static final class Frame {
        private final float t;

        private Vector2 p1;

        private Vector2 p2;

        private Frame next, prev;

        private boolean fixed;

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
        final Frame head = frame(path, start, width);
        Frame tail = head;
        final int curvatureCount = (int) (path.getLength() * 10.0F);
        final float[] curvature = new float[curvatureCount];
        for (int i = 0; i < curvatureCount; i++) {
            curvature[i] = Math.abs(path.getCurvature(i / (float) curvatureCount + start) * (width * 0.5F));
        }
        for (int i = 0; i < curvatureCount; i++) {
            if (curvature[i] > curvature[Math.floorMod(i - 1, curvatureCount)] && curvature[i] > curvature[Math.floorMod(i + 1, curvatureCount)]) {
                final float t = i / (float) curvatureCount + start;
                final Frame cur = frame(path, t, width);
                cur.fixed = true;
                tail = (cur.prev = tail).next = cur;
            }
        }
        final List<Frame> frames = Lists.newArrayList();
        if (tail != null) {
            (head.prev = tail).next = head;
            final float ta = 1.0F / steps;
            for (Frame f = head; f != head.prev; f = f.next) {
                subdivide(path, width, ta, f, f.next);
            }
            subdivide(path, width, ta, head.prev, head);
            for (Frame f = head; f != head.prev; f = f.next) {
                resolve(f, f.next);
            }
            resolve(head.prev, head);
            resolve(head, head.next);
            for (Frame f = head; f != head.prev; f = f.next) {
                frames.add(f);
            }
            frames.add(head.prev);
        }
        return frames;
    }

    private static void subdivide(final Curve path, final float width, final float target, final Frame f0, final Frame f1) {
        final float delta = Mth.deltaMod(f0.t, f1.t, 1.0F);
        if (Math.abs(delta) > target) {
            final float t = Mth.mod(f0.t - delta * 0.5F, 1.0F);
            final Frame cut = frame(path, t, width);
            (f0.next = cut).prev = f0;
            (f1.prev = cut).next = f1;
            subdivide(path, width, target, f0, cut);
            subdivide(path, width, target, cut, f1);
        }
    }

    private static Frame frame(final Curve path, final float t, final float width) {
        final Vector2 point = path.getPoint(t);
        final Vector2 normal = path.getTangent(t).rotate();
        final Vector2 v0 = normal.clone().multiply(-0.5F * width).add(point);
        final Vector2 v1 = normal.clone().multiply(0.5F * width).add(point);
        return new Frame(t, v0, v1);
    }

    private static void resolve(final Frame f0, final Frame f1) {
        final float o1 = test(f0.p1, f0.p2, f1.p1);
        final float o2 = test(f0.p1, f0.p2, f1.p2);
        final float o3 = test(f1.p1, f1.p2, f0.p1);
        final float o4 = test(f1.p1, f1.p2, f0.p2);
        if (o1 != o2 && o1 != 0.0F && o2 != 0.0F || o3 != o4 && o3 != 0.0F && o4 != 0.0F) {
            if (o1 < 0.0F || o3 > 0.0F) {
                if (f1.fixed && !f0.fixed) {
                    f0.p1.copy(f1.p1);
                } else {
                    final Vector2 r = new Vector2();
                    if (Intersections.lineLine(f0.p1, f0.prev.p1, f1.p1, f1.next.p1, r)) {
                        f0.p1.copy(r);
                    }
                    f1.p1.copy(f0.p1);
                }
            } else {
                if (f1.fixed && !f0.fixed) {
                    f0.p2.copy(f1.p2);
                } else {
                    final Vector2 r = new Vector2();
                    if (Intersections.lineLine(f0.p2, f0.prev.p2, f1.p2, f1.next.p2, r)) {
                        f0.p2.copy(r);
                    }
                    f1.p2.copy(f0.p2);
                }
            }
        }
    }
    private static float test(final Vector2 i0, final Vector2 p, final Vector2 i1) {
        return Math.signum(i1.clone().sub(i0).cross(p.clone().sub(i0)));
    }
}
