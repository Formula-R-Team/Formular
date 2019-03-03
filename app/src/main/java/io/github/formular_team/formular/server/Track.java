package io.github.formular_team.formular.server;


import com.google.common.collect.ImmutableList;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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


    public static TrackBuilder builder(Path roadPath, Shape roadShape) {
        return new TrackBuilder(roadPath, roadShape);
    }

    public static class TrackBuilder {
        private Path roadPath;
        private Shape roadShape;
        private ImmutableList<CheckPoint> checkPoints;

        TrackBuilder(Path roadPath, Shape roadShape) {
            this.roadPath = roadPath;
            this.roadShape = roadShape;
            this.checkPoints = determineCheckpoints();
        }

        public Track build() {
            return new Track(this);
        }

        //Method definitely needs to be changed to spit out real checkpoints
        private ImmutableList<CheckPoint> determineCheckpoints(){

            ArrayList<CheckPoint> list = new ArrayList<CheckPoint>();

            Vector2 point1 = this.roadPath.getPoint(1f);
            Vector2 point2 = this.roadPath.getPoint(1f);
            Vector2 point3 = this.roadPath.getPoint(2f);
            Vector2 point4 = this.roadPath.getPoint(2f);

            list.addAll(Arrays.asList(new CheckPoint(point1, point2), new CheckPoint(point3, point4)));

            return ImmutableList.copyOf(list);
        }
    }
}