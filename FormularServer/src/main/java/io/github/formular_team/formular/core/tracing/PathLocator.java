package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.Vector2;

/**
 * An object which locates any point on any line path for a given map.
 */
public interface PathLocator {
    /**
     * Returns a point which lies on a line path in the specified mapper.
     *
     * @param map the mapper to locate a line path in
     * @return position of a point on a line path
     */
    Vector2 locate(final Mapper map);
}
