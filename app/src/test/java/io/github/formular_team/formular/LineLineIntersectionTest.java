package io.github.formular_team.formular;

import org.junit.Test;

import io.github.formular_team.formular.collision.Intersections;
import io.github.formular_team.formular.math.Vector2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LineLineIntersectionTest {
    @Test
    public void should_intersect_at_zeroZero() {
        final Vector2 result = new Vector2();
        assertTrue(Intersections.lineLine(
            new Vector2(-2.0F, 0.0F), new Vector2(2.0F, 0.0F),
            new Vector2(0.0F, -2.0F), new Vector2(0.0F, 2.0F),
            result
        ));
        assertEquals(result, new Vector2(0.0F, 0.0F));
    }

    @Test
    public void should_intersect_atZeroOne() {
        final Vector2 result = new Vector2();
        assertTrue(Intersections.lineLine(
            new Vector2(-1.0F, 0.0F), new Vector2(1.0F, 2.0F),
            new Vector2(0.0F, -2.0F), new Vector2(0.0F, 2.0F),
            result
        ));
        assertEquals(result, new Vector2(0.0F, 1.0F));
    }

    @Test
    public void should_not_intersect() {
        final Vector2 result = new Vector2();
        assertFalse(Intersections.lineLine(
            new Vector2(-2.0F, 0.0F), new Vector2(2.0F, 0.0F),
            new Vector2(0.0F, 1.0F), new Vector2(0.0F, 2.0F),
            result
        ));
    }
}
