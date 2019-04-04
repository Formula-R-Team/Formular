package io.github.formular_team.formular;

import com.google.common.collect.ImmutableList;

import java.util.List;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.PathOffset;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.server.Checkpoint;
import io.github.formular_team.formular.server.FinishLineOptimizer;
import io.github.formular_team.formular.server.Track;

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
        final ImmutableList.Builder<Checkpoint> bob = ImmutableList.builder();
        final int requiredInterval = frames.size() / requiredCheckPointCount;
        for (int i = 0; i < frames.size(); i++) {
            final PathOffset.Frame fm = frames.get(i);
            bob.add(new Checkpoint(fm.getP1(), fm.getP2(), i, fm.getT(), frames.size() - i > requiredInterval && i % requiredInterval == 0));
        }
        final ImmutableList<Checkpoint> checkpoints = bob.build();
        return Track.builder()
            .setRoadPath(path)
            .setRoadWidth(this.width)
            .setRoadShape(new Shape()) // TODO: road shape
            .setCheckpoints(checkpoints)
            .build();
    }
}
