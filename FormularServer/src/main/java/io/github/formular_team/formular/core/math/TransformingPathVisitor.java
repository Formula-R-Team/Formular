package io.github.formular_team.formular.core.math;

public final class TransformingPathVisitor implements PathVisitor {
    private final PathVisitor delegate;

    private final Matrix3 transform;

    private final Vector3 out = new Vector3();

    public TransformingPathVisitor(final PathVisitor delegate, final Matrix3 transform) {
        this.delegate = delegate;
        this.transform = transform;
    }

    @Override
    public void moveTo(final float x, final float y) {
        this.prepare(x, y);
        this.delegate.moveTo(this.out.getX(), this.out.getY());
    }

    @Override
    public void lineTo(final float x, final float y) {
        this.prepare(x, y);
        this.delegate.lineTo(this.out.getX(), this.out.getY());
    }

    @Override
    public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float x, final float y) {
        this.prepare(aCP1x, aCP1y);
        final float x1 = this.out.getX();
        final float y1 = this.out.getY();
        this.prepare(aCP2x, aCP2y);
        final float x2 = this.out.getX();
        final float y2 = this.out.getY();
        this.prepare(x, y);
        final float x3 = this.out.getX();
        final float y3 = this.out.getY();
        this.delegate.bezierCurveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void closePath() {
        this.delegate.closePath();
    }

    private void prepare(final float x, final float y) {
        this.out.set(x, y, 1.0F);
        this.out.apply(this.transform);
    }
}
