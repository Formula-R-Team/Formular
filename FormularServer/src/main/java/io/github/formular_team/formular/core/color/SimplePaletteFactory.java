package io.github.formular_team.formular.core.color;

import java.util.Objects;
import java.util.Random;

public final class SimplePaletteFactory implements PaletteFactory {
    private final int size;

    private final ColorRange color;

    private SimplePaletteFactory(final Builder builder) {
        this.size = builder.size;
        this.color = builder.color;
    }

    public static Builder builder(){
        return new Builder();
    }

    @Override
    public SimpleColorPalette create(final Random rng){
        final int size = this.size;
        final Color[] palette = new Color[size];
        for (int i = 0; i < size; i++){
            final float h = this.nextFloat(rng, this.color.hue());
            final float s = this.nextFloat(rng, this.color.saturation());
            final float b = this.nextFloat(rng, this.color.value());
            palette[i] = Color.hsb(h, s, b);
        }
        return new SimpleColorPalette(palette);
    }

    private float nextFloat(final Random rng, final FloatRange range) {
        return rng.nextFloat() * (range.getUpperBound() - range.getLowerBound()) + range.getLowerBound();
    }

    public final static class Builder implements PaletteFactory.Builder {
        private int size;

        private ColorRange color;

        private Builder() {}

        @Override
        public PaletteFactory.Builder size(final int size) {
            this.size = size;
            return this;
        }

        @Override
        public PaletteFactory.Builder color(final ColorRange color) {
            this.color = Objects.requireNonNull(color);
            return this;
        }

        @Override
        public PaletteFactory build(){
            return new SimplePaletteFactory(this);
        }
    }
}
