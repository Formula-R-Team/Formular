package io.github.formular_team.formular.util;

import com.google.common.collect.Range;

public final class MorePreconditions {
    private MorePreconditions() {}

    public static <C extends Comparable> Range<C> checkBounded(final Range<C> range) {
        return checkBounded(range, "range");
    }

    public static <C extends Comparable> Range<C> checkBounded(final Range<C> range, final String desc) {
        if (!range.hasLowerBound()) {
            throw new IllegalArgumentException(String.format("%s must have lower bound", desc));
        }
        if (!range.hasUpperBound()) {
            throw new IllegalArgumentException(String.format("%s must have upper bound", desc));
        }
        return range;
    }
}
