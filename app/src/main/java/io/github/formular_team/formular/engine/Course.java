package io.github.formular_team.formular.engine;

import com.google.common.collect.ImmutableList;

import io.github.formular_team.formular.math.Point;
import io.github.formular_team.formular.math.Shape;

public interface Course {
    CourseMetadata metadata();

    Track track();

    ImmutableList<Patch> patches();

    ImmutableList<SceneryItem> sceneryItems();

    interface Track {
        Shape road();

        ImmutableList<CheckPoint> checkPoints();

        interface CheckPoint {
            Point p1();

            Point p2();
        }
    }

    interface Patch {
        NamespacedString type();

        Shape surface();
    }
}
