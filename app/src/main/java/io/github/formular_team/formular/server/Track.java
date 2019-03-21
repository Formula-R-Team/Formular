package io.github.formular_team.formular.server;


import com.google.common.collect.ImmutableList;

import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.Vector2;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Track {
    private final Path roadPath;

    private final float roadWidth;

    private final Shape roadShape;

    private final ImmutableList<Checkpoint> checkpoints;

    private Track(final TrackBuilder trackBuilder){
        this.roadPath = trackBuilder.roadPath;
        this.roadWidth = trackBuilder.roadWidth;
        this.roadShape = trackBuilder.roadShape;
        this.checkpoints = trackBuilder.checkpoints;
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

    public ImmutableList<Checkpoint> getCheckpoints() {
        return this.checkpoints;
    }

    public static TrackBuilder builder() {
        return new TrackBuilder();
    }

    public Pose getStartPlacement(final int position) {
        final float kartWidth = 2.0F, kartLength = 3.5F; // TODO: parameters from somewhere
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

    public static final class Pose {
        public final Vector2 position;

        public final float rotation;

        private Pose(final Vector2 position, final float rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }

    public static final class TrackBuilder {
        private Path roadPath;
        private float roadWidth;
        private Shape roadShape;
        private ImmutableList<Checkpoint> checkpoints;

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

        public TrackBuilder setCheckpoints(final ImmutableList<Checkpoint> checkpoints) {
            this.checkpoints = checkpoints;
            return this;
        }

        public Track build() {
            checkNotNull(this.roadPath, "Road path required");
            checkArgument(this.roadWidth > 0.0F, "Road width must be greater than zero");
            checkNotNull(this.roadShape, "Road shape required");
            checkNotNull(this.checkpoints, "Checkpoints required");
            return new Track(this);
        }
    }
}