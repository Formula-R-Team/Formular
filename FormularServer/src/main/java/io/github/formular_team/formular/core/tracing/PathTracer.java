package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.PathVisitor;

public interface PathTracer {
    void trace(final Mapper map, final PathVisitor visitor, final ResultConsumer consumer);

    interface ResultConsumer {
        void onClosed();

        void onUnclosed();

        void onFail();
    }
}
