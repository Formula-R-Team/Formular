package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Path;
import io.github.formular_team.formular.core.math.TransformingPathVisitor;
import io.github.formular_team.formular.core.math.Vector2;

public class PathFinder {
    private final PathLocator locator;

    private final PathTracer follower;

    public PathFinder(final PathLocator locator, final PathTracer follower) {
        this.locator = locator;
        this.follower = follower;
    }

    public Path find(final ImageMap image) {
        final TransformMapper map = new TransformMapper(
            new BilinearMapper(new ImageLineMap(image)),
            0.5F * image.width(),
            0.5F * image.height(),
            0.0F
        );
        final Vector2 start = this.locator.locate(map);
        map.offset(start.getX(), start.getY());
        final Path path = new Path();
        this.follower.trace(map, new TransformingPathVisitor(path, map.getMatrix()));
        return path;
    }
}
