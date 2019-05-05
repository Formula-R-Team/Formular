package io.github.formular_team.formular.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Path;
import io.github.formular_team.formular.core.math.Shape;
import io.github.formular_team.formular.core.math.Vector2;

public final class Track {
    private final Path roadPath;

    private final float roadWidth;

    private final Shape roadShape;

    private final List<? extends Checkpoint> checkpoints;

    private Track(final TrackBuilder builder){
        this.roadPath = builder.roadPath;
        this.roadWidth = builder.roadWidth;
        this.roadShape = builder.roadShape;
        this.checkpoints = builder.checkpoints;
    }

    public Path getRoadPath() {
        return this.roadPath;
    }

    public float getRoadWidth() {
        return this.roadWidth;
    }

    public Shape getRoadShape() {
        return this.roadShape;
    }

    public float getFinishLinePosition() {
        return this.checkpoints.get(0).getPosition();
    }

    public List<? extends Checkpoint> getCheckpoints() {
        return this.checkpoints;
    }

    public static TrackBuilder builder() {
        return new TrackBuilder();
    }

    public Pose getStartPlacement(final int position) {
        final float kartWidth = 1.2F, kartLength = 2.25F; // TODO: parameters from somewhere
        final int kartsPerRow = (int) ((this.roadWidth - kartWidth) / kartWidth);
        final int col = position % kartsPerRow;
        final int row = position / kartsPerRow;
        final float t = this.getFinishLinePosition() - (2.15F + kartLength * row + col / (float) kartsPerRow * kartLength) / this.roadPath.getLength();
        final Vector2 pos = this.roadPath.getPoint(t);
        final Vector2 dir = this.roadPath.getTangent(t).rotate().rotate().rotate();
        return new Pose(
            pos.add(dir.clone().multiply((col / (kartsPerRow - 1.0F) - 0.5F) * kartWidth)),
            Mth.atan2(dir.getY(), dir.getX())
        );
    }

    public static final class TrackBuilder {
        private Path roadPath;
        private float roadWidth;
        private Shape roadShape;
        private List<? extends Checkpoint> checkpoints;

        private TrackBuilder() {}

        public TrackBuilder setRoadPath(final Path roadPath) {
            this.roadPath = roadPath;
            return this;
        }

        public TrackBuilder setRoadWidth(final float roadWidth) {
            this.roadWidth = roadWidth;
            return this;
        }

        public TrackBuilder setRoadShape(final Shape roadShape) {
            this.roadShape = roadShape;
            return this;
        }

        public TrackBuilder setCheckpoints(final List<Checkpoint> checkpoints) {
            this.checkpoints = Collections.unmodifiableList(new ArrayList<>(checkpoints));
            return this;
        }

        public Track build() {
            Objects.requireNonNull(this.roadPath, "Road path required");
            if (this.roadWidth <= 0.0F) {
                throw new IllegalArgumentException("Road width must be greater than zero");
            }
            Objects.requireNonNull(this.roadShape, "Road shape required");
            Objects.requireNonNull(this.checkpoints, "Checkpoints required");
            return new Track(this);
        }
    }
}