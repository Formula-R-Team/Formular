package io.github.formular_team.formular.core;

import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.core.math.Matrix3;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Path;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.tracing.CirclePathLocator;
import io.github.formular_team.formular.core.tracing.OrientFunction;
import io.github.formular_team.formular.core.tracing.PathFinder;
import io.github.formular_team.formular.core.tracing.SimplePathTracer;
import io.github.formular_team.formular.core.tracing.SimpleStepFunction;

// TODO: good course creation api
public class CourseReader {
    private final User user;

    public CourseReader(final User user) {
        this.user = user;
    }

    public void read(final Capture capture, final Callback callback) {
        final float captureRange = capture.getRange();
        final int captureSize = capture.getResolution();
        final float courseRoadWidth = 5.5F;
        final float courseToSceneScale = 0.05F / courseRoadWidth;
        final float courseCaptureSize = captureRange / courseToSceneScale;
        final Path linePath = new Path();
        new PathFinder(
            new CirclePathLocator(25),
            new SimplePathTracer(
                new SimpleStepFunction(7, (0.5F * Mth.PI)),
                new OrientFunction(3)
            ))
            .find(capture.getImage())
            .visit(new TransformingPathVisitor(linePath, new Matrix3()
                .scale(2.0F / captureSize)
                .translate(-1.0F, -1.0F)
                .scale(courseCaptureSize, -courseCaptureSize)
            ));
        final Path path = BezierPathSimplifier.create(0.175F).simplify(linePath);
        if (path.getLength() == 0.0F || !path.isClosed()) {
            callback.fail();
        } else {
            final Course course = Course.builder()
                .setMetadata(CourseMetadata.create(this.user, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "My Circuit"))
                .setTrack(new SimpleTrackFactory(courseRoadWidth).create(path))
                .setWorldScale(courseToSceneScale)
                .build();
            if (course.getTrack().getCheckpoints().size() < 5) {
                callback.fail();
            } else {
                callback.success(course);
            }
        }
    }

    public interface Callback {
        void success(final Course course);

        void fail();
    }
}
