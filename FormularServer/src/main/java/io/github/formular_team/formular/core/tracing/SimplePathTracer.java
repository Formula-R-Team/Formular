package io.github.formular_team.formular.core.tracing;

import io.github.formular_team.formular.core.math.PathVisitor;
import io.github.formular_team.formular.core.math.Vector2;

/**
 * An implementation of a {@link PathTracer} which uses a given {@link StepFunction} and {@link OrientFunction} to trace a path in given {@link Mapper}.
 */
public class SimplePathTracer implements PathTracer {
	private final StepFunction stepFunc;

	private final OrientFunction orientFunc;

	public SimplePathTracer(final StepFunction stepFunc, final OrientFunction orientFunc) {
		this.stepFunc = stepFunc;
		this.orientFunc = orientFunc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(final Mapper map, final PathVisitor visitor, final ResultConsumer consumer) {
		final Vector2 pos = new Vector2(0.0F, 0.0F);
		float rotation = 0.0F;
		final TransformMapper view = new TransformMapper(map, pos.getX(), pos.getY(), rotation);
		visitor.moveTo(pos.getX(), pos.getY());
		for (int stepNo = 0;; stepNo++) {
			if (stepNo >= 128) {
				consumer.onFail();
				break;
			}
            final float o = this.orientFunc.getOrientation(view);
            if (!Float.isFinite(o)) {
                consumer.onUnclosed();
				break;
            }
			rotation += o;
			view.setRotation(rotation);
			final Vector2 step = view.transformVec(this.stepFunc.getStep(view));
			if (step.length() == 0.0F) {
				consumer.onUnclosed();
				break;
			}
			pos.add(step);
			view.setTranslation(pos.getX(), pos.getY());
			if (stepNo > 1 && pos.length() <= this.stepFunc.getSize() * 2) {
				if (pos.length() > this.stepFunc.getSize()) {
					visitor.lineTo(pos.getX(), pos.getY());
				}
				visitor.closePath();
				consumer.onClosed();
				break;
			}
			visitor.lineTo(pos.getX(), pos.getY());
		}
	}
}
