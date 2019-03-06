package io.github.formular_team.formular.util;

public final class Util {
    private Util() {}

    public static void reverse(final Object[] array) {
        final int count = array.length, end = count - 1, cut = count / 2;
        for (int i = 0; i < cut; i++) {
            final Object t = array[i];
            array[i] = array[end - i];
            array[end - i] = t;
        }
    }
}
