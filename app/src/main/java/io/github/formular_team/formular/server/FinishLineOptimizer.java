package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Path;

public class FinishLineOptimizer {
    public float get(final Path path) {
        final int curvatureCount = (int) (path.getLength() * 2.0F);
        final float[] curve = new float[curvatureCount];
        for (int i = 0; i < curvatureCount; i++) {
            curve[i] = Math.abs(path.getCurvature(i / (float) curvatureCount));
        }
        final float[] curveDeltas = new float[curvatureCount];
        for (int i = 0; i < curvatureCount; i++) {
            curveDeltas[i] = Math.signum(curve[(i + 1) % curvatureCount] - curve[i]);
        }
        final int[] sections = new int[curvatureCount];
        int sectionCount = 0;
        for (int i = 0; i < curvatureCount; i++) {
            if (curveDeltas[(i + 1) % curvatureCount] != curveDeltas[i]) {
                sections[sectionCount++] = i + 1;
            }
        }
        int section = -1;
        int maxSectionLength = 0;
        for (int i = 0; i < sectionCount; i++) {
            final int start = sections[i];
            int end = sections[(i + 1) % sectionCount];
            if (end < start) {
                end += curvatureCount;
            }
            if (end - start > maxSectionLength) {
                maxSectionLength = end - start;
            }
        }
        float bestRank = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < sectionCount; i++) {
            final int start = sections[i];
            int end = sections[(i + 1) % sectionCount];
            if (end < start) {
                end += curvatureCount;
            }
            final int length = end - start;
            final float K = curve[start % curvatureCount];
            float Ex = 0.0F, Ex2 = 0.0F;
            for (int j = start; j < end; j++) {
                final float x = curve[j % curvatureCount];
                Ex += x - K;
                Ex2 += (x - K) * (x - K);
            }
            final float rank = (length / (float) maxSectionLength) - 2.0F * ((Ex2 - (Ex * Ex) / length) / length);
            if (rank > bestRank) {
                section = i;
                bestRank = rank;
            }
        }
        final int start = sections[section];
        int end = sections[(section + 1) % sectionCount];
        if (end < start) {
            end += curvatureCount;
        }
        return (end / (float) curvatureCount) % 1.0F;
    }
}
