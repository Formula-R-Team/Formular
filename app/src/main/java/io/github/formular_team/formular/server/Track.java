package io.github.formular_team.formular.server;


import com.google.common.collect.ImmutableList;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Shape;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Track {
    private final Path roadPath;

    private final Shape roadShape;

    private final ImmutableList<CheckPoint> checkPoints;

    private Track(final TrackBuilder trackBuilder){
        this.roadPath = trackBuilder.roadPath;
        this.roadShape = trackBuilder.roadShape;
        this.checkPoints = trackBuilder.checkPoints;
    }

    public Path getRoadPath() {
        return this.roadPath;
    }

    public Shape getRoadShape() {
        return this.roadShape;
    }

    public ImmutableList<CheckPoint> getCheckPoints() {
        return this.checkPoints;
    }

    public static TrackBuilder builder() {
        return new TrackBuilder();
    }

    public static final class TrackBuilder {
        private Path roadPath;
        private Shape roadShape;
        private ImmutableList<CheckPoint> checkPoints;

        private TrackBuilder() {}

        public TrackBuilder setRoadPath(final Path roadPath) {
            this.roadPath = roadPath;
            return this;
        }

        public TrackBuilder setRoadShape(final Shape roadShape) {
            this.roadShape = roadShape;
            return this;
        }

        public TrackBuilder setCheckPoints(final ImmutableList<CheckPoint> checkPoints) {
            this.checkPoints = checkPoints;
            return this;
        }

        public Track build() {
            checkNotNull(this.roadPath, "Road path required");
            checkNotNull(this.roadShape, "Road shape required");
            checkNotNull(this.checkPoints, "Check points required");
            return new Track(this);
        }
    }
}