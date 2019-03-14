package io.github.formular_team.formular;

import org.junit.Test;

import io.github.formular_team.formular.collision.Intersections;
import io.github.formular_team.formular.math.Vector2;

import static org.junit.Assert.*;

public class IntersectionsTest {
    @Test
    public void should_hit_when_lineEnterAndExit() {
        assertTrue(Intersections.lineCircle(new Vector2(-2.0F, 0.0F), new Vector2(2.0F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }

    @Test
    public void should_hit_when_lineEnter() {
        assertTrue(Intersections.lineCircle(new Vector2(2.0F, 0.0F), new Vector2(0.0F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }

    @Test
    public void should_hit_when_lineExit() {
        assertTrue(Intersections.lineCircle(new Vector2(0.0F, 0.0F), new Vector2(2.0F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }

    @Test
    public void should_hit_when_lineInside() {
        assertTrue(Intersections.lineCircle(new Vector2(-0.5F, 0.0F), new Vector2(0.5F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }

    @Test
    public void should_miss_when_lineBefore() {
        assertFalse(Intersections.lineCircle(new Vector2(-2.0F, 0.0F), new Vector2(-1.5F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }

    @Test
    public void should_miss_when_lineAfter() {
        assertFalse(Intersections.lineCircle(new Vector2(1.5F, 0.0F), new Vector2(2.0F, 0.0F), new Vector2(0.0F, 0.0F), 1.0F));
    }
}