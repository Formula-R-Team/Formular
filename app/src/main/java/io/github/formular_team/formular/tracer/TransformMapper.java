package io.github.formular_team.formular.tracer;

public final class TransformMapper implements Mapper {
    private final Mapper image;

    private double x;

    private double y;

    private double cosRotation;

    private double sinRotation;

    public TransformMapper(final Mapper image, final double x, final double y, final double rotation) {
        this(image, x, y, Math.cos(rotation), Math.sin(rotation));
    }

    private TransformMapper(final Mapper image, final double x, final double y, final double cosRotation, final double sinRotation) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.cosRotation = cosRotation;
        this.sinRotation = sinRotation;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public void setRotation(final double rotation) {
        this.cosRotation = Math.cos(rotation);
        this.sinRotation = Math.sin(rotation);
    }

    @Override
    public double get(final double x, final double y) {
        final Vec2 v = this.transformPoint(new Vec2(x, y));
        return this.image.get(v.getX(), v.getY());
    }

    public Vec2 transformPoint(final Vec2 vector) {
        final Vec2 result = this.transformVec(vector);
        result.add(new Vec2(this.x, this.y));
        return result;
    }

    public Vec2 transformVec(final Vec2 vector) {
        return new Vec2(vector.getX() * this.cosRotation - vector.getY() * this.sinRotation, vector.getY() * this.cosRotation + vector.getX() * this.sinRotation);
    }
}
