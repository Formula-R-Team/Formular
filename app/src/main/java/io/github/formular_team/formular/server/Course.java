package io.github.formular_team.formular.server;

import com.google.common.collect.ImmutableList;

import io.github.formular_team.formular.math.Shape;

public interface Course {
    CourseMetadata metadata();

    Track track();

    ImmutableList<Patch> patches();

    ImmutableList<SceneryItem> sceneryItems();

    interface Patch {
        NamespacedString registryName();

        Shape surface();
    }
}
