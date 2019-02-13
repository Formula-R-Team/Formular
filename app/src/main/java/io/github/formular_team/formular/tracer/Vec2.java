package io.github.formular_team.formular.tracer;

public final class Vec2 {
    private float x;

    private float y;

    public Vec2(final Vec2 other) {
        this(other.getX(), other.getY());
    }

    public Vec2(final double theta) {
        this(Math.cos(theta), Math.sin(theta));
    }

    public Vec2(final double x, final double y) {
        this((float) x, (float) y);
    }

    public Vec2(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public void set(final float x, final float y) {
        this.setX(x);
        this.setY(y);
    }

    public void set(final Vec2 other) {
        this.set(other.getX(), other.getY());
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void add(final Vec2 other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void mul(final double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public void mul(final Vec2 other) {
        this.x *= other.x;
        this.y *= other.y;
    }

    public void rotate(final double theta) {
        final Vec2 v = new Vec2(theta);
        this.set(this.x * v.x - this.y * v.y, this.y * v.x + this.x * v.y);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    public double length() {
        return Math.hypot(this.x, this.y);
    }

    public void minus(final Vec2 pos) {
        this.x -= pos.x;
        this.y -= pos.y;
    }
}
