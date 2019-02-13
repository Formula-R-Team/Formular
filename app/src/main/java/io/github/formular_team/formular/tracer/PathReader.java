package io.github.formular_team.formular.tracer;

public class PathReader {
	private final StepFunction stepFunc;

	private final ToDoubleMapFunction orientFunc;

	public PathReader(final StepFunction stepFunc, final ToDoubleMapFunction orientFunc) {
		this.stepFunc = stepFunc;
		this.orientFunc = orientFunc;
	}

	public boolean read(final Mapper map, final PathBuilder path) {
		final Vec2 pos = new Vec2(0.0D, 0.0D);
		double rotation = 0.0D;
		final TransformMapper view = new TransformMapper(map, pos.getX(), pos.getY(), rotation);
		path.moveTo(pos.getX(), pos.getY());
		for (int stepNo = 0;; stepNo++) {
			if (stepNo >= 128) {
				break; // too long
			}
			rotation += this.orientFunc.orient(view);
			view.setRotation(rotation);
			final Vec2 step = view.transformVec(this.stepFunc.step(view));
			if (step.length() == 0.0D) {
				break; // dead end
			}
			pos.add(step);
			view.setX(pos.getX());
			view.setY(pos.getY());
			if (stepNo > 0 && pos.length() <= this.stepFunc.getSize()) {
				path.closePath();
				break;
			}
			path.lineTo(pos.getX(), pos.getY());
		}
		return true;
	}
}
