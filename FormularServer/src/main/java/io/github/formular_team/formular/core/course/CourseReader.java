package io.github.formular_team.formular.core.course;

import io.github.formular_team.formular.core.BezierPathSimplifier;
import io.github.formular_team.formular.core.SimpleTrackFactory;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.math.Matrix3;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.tracing.CirclePathLocator;
import io.github.formular_team.formular.core.tracing.OrientFunction;
import io.github.formular_team.formular.core.tracing.PathFinder;
import io.github.formular_team.formular.core.tracing.SimplePathTracer;
import io.github.formular_team.formular.core.tracing.SimpleStepFunction;

// TODO: good course creation api
public class CourseReader {
    public void read(final Capture capture, final CourseMetadata metadata, final ResultConsumer consumer) {
        final float courseRoadWidth = 5.0F;
        final float courseToSceneScale = 0.06F / courseRoadWidth;
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
                    final Path linePath = new Path();
                    path.visit(new TransformingPathVisitor(linePath, new Matrix3()
                        .scale(2.0F / capture.getResolution())
                        .translate(-1.0F, -1.0F)
                        .scale(courseCaptureSize, -courseCaptureSize)
                    ));
                    final Path simplifiedPath = BezierPathSimplifier.create(0.175F).simplify(linePath);
                    final Track track = new SimpleTrackFactory(courseRoadWidth).create(simplifiedPath);
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

    public interface ResultConsumer {
        void onSuccess(final Course course);

        void onFail();
    }
}
