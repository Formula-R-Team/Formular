package io.github.formular_team.formular.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.github.formular_team.formular.math.Shape;

public final class Course {
    private final CourseMetadata metadata;

    private final float size;

    private final Track track;

    private final ImmutableList<Patch> patches;

    private final ImmutableList<SceneryItem> sceneryItems;

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

    public ImmutableList<Patch> getPatches() {
        return this.patches;
    }

    public ImmutableList<SceneryItem> getSceneryItems() {
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

        private ImmutableList<Patch> patches = ImmutableList.of();

        private ImmutableList<SceneryItem> sceneryItems = ImmutableList.of();

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

        public Builder setPatches(final ImmutableList<Patch> patches) {
            this.patches = patches;
            return this;
        }

        public Builder setSceneryItems(final ImmutableList<SceneryItem> sceneryItems) {
            this.sceneryItems = sceneryItems;
            return this;
        }

        public Course build() {
            Preconditions.checkNotNull(this.metadata, "Metadata must not be null");
            Preconditions.checkArgument(this.size > 0.0F, "Size must be greater than zero");
            Preconditions.checkNotNull(this.track, "Track must not be null");
            return new Course(this);
        }
    }
}
