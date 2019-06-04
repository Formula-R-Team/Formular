package io.github.formular_team.formular.core.course;

import io.github.formular_team.formular.core.BezierPathSimplifier;
import io.github.formular_team.formular.core.SimpleTrackFactory;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.math.Matrix3;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.tracing.CirclePathLocator;
import io.github.formular_team.formular.core.tracing.PCAOrientFunction;
import io.github.formular_team.formular.core.tracing.PathFinder;
import io.github.formular_team.formular.core.tracing.SimplePathTracer;
import io.github.formular_team.formular.core.tracing.SimpleStepFunction;

// TODO: good course creation api
public class CourseReader {
    final float courseToSceneScale = 0.012F;

    public interface PoseConsumer {
        void onSuccess(final Path path, final PathPose pose);

        void onFail();
    }

    public void readPose(final Capture capture, final PoseConsumer consumer) {
        final float courseCaptureSize = capture.getRadius() / this.courseToSceneScale;
        new PathFinder(
            new CirclePathLocator(25),
            new SimplePathTracer(
                new SimpleStepFunction(7, (0.5F * Mth.PI)),
                new PCAOrientFunction(3)
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
                    final PathPose pose = PathPose.get(worldPath, CourseReader.this.courseToSceneScale);
                    worldPath.visit(new TransformingPathVisitor(normalPath, new Matrix3()
                        .multiply(new Matrix3().translate(-pose.getPosition().getX(), -pose.getPosition().getY()))
                        .multiply(new Matrix3().rotate(-pose.getEllipse().getAngle()))
                    ));
                    consumer.onSuccess(normalPath, pose);
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

    public void createCourse(final CourseMetadata metadata, final Path path, final ResultConsumer consumer) {
        final Path simplifiedPath = BezierPathSimplifier.create(0.175F).simplify(path);
        final Track track = new SimpleTrackFactory(5.0F).create(simplifiedPath);
        if (track.getCheckpoints().size() >= 5) {
            final Course course = Course.builder()
                .setMetadata(metadata)
                .setTrack(track)
                .build();
            consumer.onSuccess(course);
        } else {
            consumer.onFail();
        }
    }

    public interface ResultConsumer {
        void onSuccess(final Course course);

        void onFail();
    }
}
