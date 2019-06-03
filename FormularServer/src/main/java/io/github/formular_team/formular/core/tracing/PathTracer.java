package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.PathVisitor;

/**
 * An object which traces a line read from {@link Mapper} into a {@link PathVisitor}.
 */
public interface PathTracer {
    /**
     * Traces a line located within the specified {@link Mapper}, visiting the path with the given {@link PathVisitor} and providing the result to the specified {@link ResultConsumer}.
     * <p/>
     * A trace operation may complete with the construction of closed path, unclosed path, or a failure.
     * A closed or unclosed result indicates a valid state of the given path visitor, while undefined state is supplied for a failure.
     * A closed path is produced when a line can be traced from and back to the starting location, while an unclosed path is produced when the tracing line is lost, a failure occurs when the path traced is not believed to be valid.
     *
     * @param map mapper to read from
     * @param visitor path visitor to build output path
     * @param consumer consumer to receive the results of the trace operation
     */
    void trace(final Mapper map, final PathVisitor visitor, final ResultConsumer consumer);

    /**
     * An object which consumes the result of a trace operation.
     */
    interface ResultConsumer {
        /**
         * Result when a path is completed and forms closed loop
         */
        void onClosed();

        /**
         * Result when a path is completed and does not form a closed loop
         */
        void onUnclosed();

        /**
         * Result when a path is unable to be adequately traced
         */
        void onFail();
    }
}
