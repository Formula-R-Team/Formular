package io.github.formular_team.formular.core.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.formular_team.formular.core.NamespacedString;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.math.curve.Shape;

public final class Course {
    private final CourseMetadata metadata;

    private final Track track;

    private final List<? extends Patch> patches;

    private final List<? extends SceneryItem> sceneryItems;

    private float worldScale;

    private Course(final Builder builder) {
        this.metadata = builder.metadata;
        this.track = builder.track;
        this.patches = builder.patches;
        this.sceneryItems = builder.sceneryItems;
        this.worldScale = builder.worldScale;
    }

    public CourseMetadata getMetadata() {
        return this.metadata;
    }

    public Track getTrack() {
        return this.track;
    }

    public List<? extends Patch> getPatches() {
        return this.patches;
    }

    public List<? extends SceneryItem> getSceneryItems() {
        return this.sceneryItems;
    }

    public float getWorldScale() {
        return this.worldScale;
    }

    interface Patch {
        NamespacedString registryName();

        Shape surface();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CourseMetadata metadata;

        private Track track;

        private List<? extends Patch> patches = Collections.emptyList();

        private List<? extends SceneryItem> sceneryItems = Collections.emptyList();

        private float worldScale = 1.0F;

        private Builder() {}

        public Builder setMetadata(final CourseMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setTrack(final Track track) {
            this.track = track;
            return this;
        }

        public Builder setPatches(final List<Patch> patches) {
            this.patches = Collections.unmodifiableList(new ArrayList<>(patches));
            return this;
        }

        public Builder setSceneryItems(final List<SceneryItem> sceneryItems) {
            this.sceneryItems = Collections.unmodifiableList(new ArrayList<>(sceneryItems));
            return this;
        }

        public Builder setWorldScale(final float worldScale) {
            if (worldScale <= 0.0F) {
                throw new IllegalStateException("World scale must be positive: " + worldScale);
            }
            this.worldScale = worldScale;
            return this;
        }

        public Course build() {
            Objects.requireNonNull(this.metadata, "Metadata must not be null");
            Objects.requireNonNull(this.track, "Track must not be null");
            return new Course(this);
        }
    }
}
