package io.github.formular_team.formular.server;

import com.google.common.collect.Range;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.formular_team.formular.util.MorePreconditions.checkBounded;

public final class KartDefinition {
    private final String name;

    private final float size;

    private final float mass;

    private final float acceleration;

    private KartDefinition(final Builder builder){
        this.name = builder.name;
        this.size = builder.size;
        this.mass = builder.mass;
        this.acceleration = builder.acceleration;
    }

    public String getName(){return this.name;}

    public float getSize(){return this.size;}

    public float getMass(){return this.mass;}

    public float getAcceleration(){return this.acceleration;}

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {

        private String name;
        private float size;
        private float mass;
        private float acceleration;

        private Builder(){}

        public Builder name(final String name){
            checkNotNull(name);
            this.name = name;
            return this;
        }

        public Builder size(final float size){
            checkNotNull(size);
            this.size = size;
            return this;
        }

        public Builder mass(final float mass){
            checkNotNull(mass);
            this.mass = mass;
            return this;
        }

        public Builder acceleration(final float acceleration){
            checkNotNull(acceleration);
            this.acceleration = acceleration;
            return this;
        }

        public KartDefinition build(){return new KartDefinition(this);}
    }
}
