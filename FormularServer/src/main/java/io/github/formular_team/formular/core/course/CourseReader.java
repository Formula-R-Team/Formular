package io.github.formular_team.formular.core.course;

import java.util.List;

import io.github.formular_team.formular.core.BezierPathSimplifier;
import io.github.formular_team.formular.core.SimpleTrackFactory;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.math.Matrix3;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.tracing.CirclePathLocator;
import io.github.formular_team.formular.core.tracing.OrientFunction;
import io.github.formular_team.formular.core.tracing.PCA;
import io.github.formular_team.formular.core.tracing.PathFinder;
import io.github.formular_team.formular.core.tracing.SimplePathTracer;
import io.github.formular_team.formular.core.tracing.SimpleStepFunction;

// TODO: good course creation api
public class CourseReader {
    public void read(final Capture capture, final CourseMetadata metadata, final ResultConsumer consumer) {
        final float courseToSceneScale = 0.012F;
        final float courseCaptureSize = capture.getRadius() / courseToSceneScale;
        new PathFinder(
            new CirclePathLocator(25),
            new SimplePathTracer(
                new SimpleStepFunction(7, (0.5F * Mth.PI)),
                new OrientFunction(3)
            ))
            .find(capture.getImage(), new PathFinder.ResultConsumer() {
                @Override
                public void onClosed(final Path path) {
                    final Path worldPath = new Path();
                    path.visit(new TransformingPathVisitor(worldPath, new Matrix3()
                        .multiply(new Matrix3().scale(2.0F / capture.getResolution()))
                        .multiply(new Matrix3().translate(-1.0F, -1.0F))
                        .multiply(new Matrix3().scale(courseCaptureSize, -courseCaptureSize))
                    ));
                    final Path normalPath = new Path();
                    final TrackPose pose = get(worldPath);
                    worldPath.visit(new TransformingPathVisitor(normalPath, new Matrix3()
                        .multiply(new Matrix3().translate(-pose.position.getX(), -pose.position.getY()))
                        .multiply(new Matrix3().rotate(-pose.ellipse.getAngle()))
                    ));
                    final Path simplifiedPath = BezierPathSimplifier.create(0.175F).simplify(normalPath);
                    final Track track = new SimpleTrackFactory(5.0F).create(simplifiedPath);
                    if (track.getCheckpoints().size() >= 5) {
                        final Course course = Course.builder()
                            .setMetadata(metadata)
                            .setTrack(track)
                            .setWorldScale(courseToSceneScale)
                            .build();
                        consumer.onSuccess(course);
                    } else {
                        consumer.onFail();
                    }
                }

                @Override
                public void onUnclosed(final Path path) {
                    consumer.onFail();
                }

                @Override
                public void onFail() {
                    consumer.onFail();
                }
            });
    }

    static class TrackPose {
        private final Vector2 position;

        private final PCA.Ellipse ellipse;

        TrackPose(final Vector2 position, final PCA.Ellipse ellipse) {
            this.position = position;
            this.ellipse = ellipse;
        }
    }

    public static TrackPose get(final Path path) {
        final List<Vector2> points = path.getPoints(false);
        final int n = points.size();
        final float[] x = new float[n], y = new float[n], w = new float[n];
        final Vector2 avg = new Vector2();
        for (int i = 0; i < n ; i++) {
            final Vector2 point = points.get(i);
            x[i] = point.getX();
            y[i] = point.getY();
            w[i] = 1.0F;
            avg.add(point);
        }
        avg.divide(n);
        return new TrackPose(avg, PCA.get(x, y, w, n));
    }

    public interface ResultConsumer {
        void onSuccess(final Course course);

        void onFail();
    }
}
