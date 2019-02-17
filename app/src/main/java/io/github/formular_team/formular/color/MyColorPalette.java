package io.github.formular_team.formular.color;

import java.util.stream.IntStream;

public final class MyColorPalette implements ColorPalette {

    private int [] _palette;

    MyColorPalette (int [] palette){
        _palette = palette;
    }

    @Override
    public int size() {
        return _palette.length;
    }

    @Override
    public boolean isEmpty() {
        if(_palette.length == 0 || _palette == null){
            return true;
        }
        return false;
    }

    @Override
    public int get(int index) {
        return _palette[index];
    }

    @Override
    public IntStream stream() {
        return null;
    }
}
