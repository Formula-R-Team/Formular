package io.github.formular_team.formular.color;

import android.graphics.Color;

import com.google.common.collect.Range;

import java.util.Random;
import java.util.stream.IntStream;

public final class MyPaletteFactory implements PaletteFactory {

    private Range<Integer> _size;
    private ColorRange _color;

    public MyPaletteFactory(Range<Integer> size, ColorRange color){
        _size = size;
        _color = color;
    }

    public static PaletteFactoryBuilder builder(){
        return new PaletteFactoryBuilder();
    }

    public MyColorPalette create(final Random rng){
        int color;
        Float saturation = Float.valueOf(-1);
        Float hue = Float.valueOf(-1);
        Float value = Float.valueOf(-1);
        int sizeRange = _size.upperEndpoint() - _size.lowerEndpoint();
        int [] ara = new int[sizeRange];
        float [] hsv = new float [3];
        for(int i = 0; i < sizeRange; i++){
            while(hue < _color.hue().lowerEndpoint() || hue > _color.hue().upperEndpoint()){
                hue = rng.nextFloat();
                hue = hue * 360;
            }
            while(saturation < _color.saturation().lowerEndpoint() || saturation > _color.saturation().upperEndpoint()){
                saturation = rng.nextFloat();
            }
            while(value < _color.value().lowerEndpoint() || value > _color.value().upperEndpoint()){
                value = rng.nextFloat();
            }

            hsv[0] = hue;
            hsv[1] = saturation;
            hsv[2] = value;
            color = Color.HSVToColor(hsv);
            ara[i] = color;
        }
        MyColorPalette colorPalette = new MyColorPalette(ara);
        return colorPalette;
    }

    public final static class PaletteFactoryBuilder implements PaletteFactory.Builder{
        private Range<Integer> _size;
        private ColorRange _color;

        private PaletteFactoryBuilder(){}

        @Override
        public Builder size(Range<Integer> size) {
            _size = size;
            return this;
        }

        @Override
        public Builder color(final ColorRange color) {
            _color = color;
            return this;
        }

        public PaletteFactory build(){
            return new MyPaletteFactory(_size, _color);
        }
    }



}
