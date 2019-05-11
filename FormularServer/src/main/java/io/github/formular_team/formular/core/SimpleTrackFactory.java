package io.github.formular_team.formular.core;

import java.util.ArrayList;
import java.util.List;

import io.github.formular_team.formular.core.course.track.FinishLineOptimizer;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.course.track.TrackFactory;
import io.github.formular_team.formular.core.math.PathOffset;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.math.curve.Shape;

public class SimpleTrackFactory implements TrackFactory {
    private final float width;

    public SimpleTrackFactory(final float width) {
        this.width = width;
    }

    @Override
    public Track create(final Path path) {
        final float finishLinePosition = new FinishLineOptimizer().get(path);
        final List<PathOffset.Frame> frames = PathOffset.createFrames(path, finishLinePosition, (int) (path.getLength() * 0.75F), this.width + 0.75F);
        final int requiredCheckPointCount = 8;
        final List<Checkpoint> checkpoints = new ArrayList<>();
        final int requiredInterval = frames.size() / requiredCheckPointCount;
        for (int i = 0; i < frames.size(); i++) {
            final PathOffset.Frame fm = frames.get(i);
            checkpoints.add(new Checkpoint(fm.getP1(), fm.getP2(), i, fm.getT(), frames.size() - i > requiredInterval && i % requiredInterval == 0));
        }
        return Track.builder()
            .setRoadPath(path)
            .setRoadWidth(this.width)
            .setRoadShape(new Shape()) // TODO: road shape
            .setCheckpoints(checkpoints)
            .build();
    }
}
