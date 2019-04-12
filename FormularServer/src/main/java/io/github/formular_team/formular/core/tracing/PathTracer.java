package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.PathVisitor;

public interface PathTracer {
    boolean trace(final Mapper map, final PathVisitor visitor);
}
