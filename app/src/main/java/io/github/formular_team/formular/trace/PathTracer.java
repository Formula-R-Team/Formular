package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.PathVisitor;

public interface PathTracer {
    boolean trace(final Mapper map, final PathVisitor visitor);
}
