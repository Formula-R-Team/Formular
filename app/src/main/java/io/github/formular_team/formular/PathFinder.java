package io.github.formular_team.formular;

import android.graphics.Bitmap;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.TransformingPathVisitor;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.trace.BilinearMapper;
import io.github.formular_team.formular.trace.ImageLineMap;
import io.github.formular_team.formular.trace.PathFollower;
import io.github.formular_team.formular.trace.TransformMapper;

public class PathFinder {
    private final PathLocator locator;

    private final PathFollower follower;

    public PathFinder(final PathLocator locator, final PathFollower follower) {
        this.locator = locator;
        this.follower = follower;
    }

    public Path find(final Bitmap capture) {
        final TransformMapper mapper = new TransformMapper(
            new BilinearMapper(new ImageLineMap(new BitmapImageMap(capture))),
            0.5F * capture.getWidth(),
            0.5F * capture.getHeight(),
            0.0F
        );
        final Vector2 start = this.locator.locate(mapper);
        mapper.offset(start.getX(), start.getY());
        final Path path = new Path();
        this.follower.follow(mapper, new TransformingPathVisitor(path, mapper.getMatrix()));
        return path;
    }
}
