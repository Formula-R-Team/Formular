package io.github.formular_team.formular.trace;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.Vector2;

public class PathReader {
	private final StepFunction stepFunc;

	private final ToDoubleMapFunction orientFunc;

	public PathReader(final StepFunction stepFunc, final ToDoubleMapFunction orientFunc) {
		this.stepFunc = stepFunc;
		this.orientFunc = orientFunc;
	}

	public boolean read(final Mapper map, final Path.Builder path) {
		final Vector2 pos = new Vector2(0.0F, 0.0F);
		float rotation = 0.0F;
		final TransformMapper view = new TransformMapper(map, pos.x(), pos.y(), rotation);
		path.moveTo(pos);
		for (int stepNo = 0;; stepNo++) {
			if (stepNo >= 128) {
				break; // too long
			}
			rotation += this.orientFunc.orient(view);
			view.setRotation(rotation);
			final Vector2 step = view.transformVec(this.stepFunc.step(view));
			if (step.length() == 0.0F) {
				break; // dead end
			}
			pos.add(step);
			view.setX(pos.x());
			view.setY(pos.y());
			if (stepNo > 0 && pos.length() <= this.stepFunc.getSize()) {
				path.closePath();
				break;
			}
			path.lineTo(pos);
		}
		return true;
	}
}
