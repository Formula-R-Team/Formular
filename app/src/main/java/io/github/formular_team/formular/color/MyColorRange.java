package io.github.formular_team.formular.color;

import com.google.common.collect.Range;

public final class MyColorRange implements PaletteFactory.ColorRange{

    private Range<Float> _red;
    private Range<Float> _green;
    private Range<Float> _blue;
    private Range<Float> _hue;
    private Range<Float> _saturation;
    private Range<Float> _value;

    public MyColorRange(Range<Float> red, Range<Float> green,Range<Float> blue, Range<Float> hue,Range<Float> saturation, Range<Float> value){
        _red = red;
        _green = green;
        _blue = blue;
        _hue = hue;
        _saturation = saturation;
        _value = value;
    }

    public Range<Float> red(){return _red;}
    public Range<Float> green(){return _green;}
    public Range<Float> blue(){return _blue;}
    public Range<Float> hue(){return _hue;}
    public Range<Float> saturation(){return _saturation;}
    public Range<Float> value(){return _value;}

    public static ColorRangeBuilder builder(){
        return new ColorRangeBuilder();
    }

    public final static class ColorRangeBuilder implements PaletteFactory.ColorRange.Builder{

        private Range<Float> _red;
        private Range<Float> _green;
        private Range<Float> _blue;
        private Range<Float> _hue;
        private Range<Float> _saturation;
        private Range<Float> _value;

        private ColorRangeBuilder(){}

        public Builder red(final Range<Float> red){
            _red = red;
            return this;
        }

        public Builder green(final Range<Float> green){
            _green = green;
            return this;
        }

        public Builder blue(final Range<Float> blue){
            _blue = blue;
            return this;
        }

        public Builder hue(final Range<Float> hue){
            _hue = hue;
            return this;
        }

        public Builder saturation(final Range<Float> saturation){
            _saturation = saturation;
            return this;
        }

        public Builder value(final Range<Float> value){
            _value = value;
            return this;
        }

        public PaletteFactory.ColorRange build(){
            return new MyColorRange(_red,_green,_blue,_hue,_saturation,_value);
        }

    }

}