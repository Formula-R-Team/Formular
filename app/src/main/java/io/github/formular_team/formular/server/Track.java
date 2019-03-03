package io.github.formular_team.formular.server;


import com.google.common.collect.ImmutableList;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.Vector2;

import static com.google.common.base.Preconditions.checkNotNull;

public class Track {

    private static class CheckPoint {
        Vector2 p1, p2;

        CheckPoint(Vector2 p1, Vector2 p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private final Path roadPath;
    private final Shape roadShape;
    private final ImmutableList<CheckPoint> checkPoints;

    private Track(final TrackBuilder trackBuilder){
        this.roadPath = trackBuilder.roadPath;
        this.roadShape = trackBuilder.roadShape;
        this.checkPoints = trackBuilder.checkPoints;
    }

    public Path getRoadPath(){return this.roadPath;}

    public Shape getRoadShape(){return this.roadShape;}

    public ImmutableList<CheckPoint> getCheckPoints(){return this.checkPoints;}


    public static TrackBuilder builder() {
        return new TrackBuilder();
    }

    public static final class TrackBuilder {
        private Path roadPath;
        private Shape roadShape;
        private ImmutableList<CheckPoint> checkPoints;

        private TrackBuilder() {}

        public TrackBuilder roadPath(final Path roadPath) {
            checkNotNull(this.roadPath, "Road Path Required for TrackBuilder");
            this.roadPath = roadPath;
            return this;
        }

        public TrackBuilder roadShape(final Shape roadShape) {
            checkNotNull(this.roadShape, "Road Shape Required for TrackBuilder");
            this.roadShape = roadShape;
            return this;
        }

        public TrackBuilder checkPoints(final ImmutableList<CheckPoint> checkPoints) {
            checkNotNull(this.checkPoints, "Checkpoints are Required for TrackBuilder");
            this.checkPoints = checkPoints;
            return this;
        }

        public Track build() {
            return new Track(this);
        }
    }
}