package io.github.formular_team.formular.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.formular_team.formular.core.math.Shape;

public final class Course {
    private final CourseMetadata metadata;

    private final float size;

    private final Track track;

    private final List<? extends Patch> patches;

    private final List<? extends SceneryItem> sceneryItems;

    private Course(final Builder builder) {
        this.metadata = builder.metadata;
        this.size = builder.size;
        this.track = builder.track;
        this.patches = builder.patches;
        this.sceneryItems = builder.sceneryItems;
    }

    public CourseMetadata getMetadata() {
        return this.metadata;
    }

    public float getSize() {
        return this.size;
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

    interface Patch {
        NamespacedString registryName();

        Shape surface();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CourseMetadata metadata;

        private float size;

        private Track track;

        private List<? extends Patch> patches = Collections.emptyList();

        private List<? extends SceneryItem> sceneryItems = Collections.emptyList();

        private Builder() {}

        public Builder setMetadata(final CourseMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setSize(final float size) {
            this.size = size;
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

        public Course build() {
            Objects.requireNonNull(this.metadata, "Metadata must not be null");
            if (this.size <= 0.0F) {
                throw new IllegalArgumentException("Size must be greater than zero");
            }
            Objects.requireNonNull(this.track, "Track must not be null");
            return new Course(this);
        }
    }
}
