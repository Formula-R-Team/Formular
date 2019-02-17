package io.github.formular_team.formular.color;

import android.graphics.Color;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.formular_team.formular.util.MorePreconditions.checkBounded;

public final class MyPaletteFactory implements PaletteFactory {
    private final Range<Integer> size;

    private final ColorRange color;

    private MyPaletteFactory(final PaletteFactoryBuilder builder) {
        this.size = builder.size;
        this.color = builder.color;
    }

    public static PaletteFactoryBuilder builder(){
        return new PaletteFactoryBuilder();
    }

    @Override
    public MyColorPalette create(final Random rng){
        final int size = this.nextInt(rng, this.size);
        final int[] palette = new int[size];
        final float[] hsv = new float[3];
        for (int i = 0; i < size; i++){
            hsv[0] = this.nextFloat(rng, this.color.hue());
            hsv[1] = this.nextFloat(rng, this.color.saturation());
            hsv[2] = this.nextFloat(rng, this.color.value());
            palette[i] = Color.HSVToColor(hsv);
        }
        return new MyColorPalette(palette);
    }

    private int nextInt(final Random rng, final Range<Integer> range) {
        int lower = range.lowerEndpoint();
        if (range.lowerBoundType() == BoundType.OPEN) {
            lower++;
        }
        int upper = range.upperEndpoint();
        if (range.upperBoundType() == BoundType.OPEN) {
            upper--;
        }
        return rng.nextInt(1 + upper - lower) + lower;
    }

    private float nextFloat(final Random rng, final Range<Float> range) {
        return rng.nextFloat() * (range.upperEndpoint() - range.lowerEndpoint()) + range.lowerEndpoint();
    }

    public final static class PaletteFactoryBuilder implements PaletteFactory.Builder {
        private Range<Integer> size;

        private ColorRange color;

        private PaletteFactoryBuilder() {}

        @Override
        public Builder size(final Range<Integer> size) {
            checkNotNull(size);
            checkBounded(size);
            this.size = size;
            return this;
        }

        @Override
        public Builder color(final ColorRange color) {
            this.color = checkNotNull(color);
            return this;
        }

        public PaletteFactory build(){
            return new MyPaletteFactory(this);
        }
    }
}
