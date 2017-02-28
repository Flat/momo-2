package io.ph.bot.procedural;

public interface ProceduralInterface {
	public default String getTitle() {
		return this.getClass().getAnnotation(ProceduralAnnotation.class).title();
	}
	public default String[] getSteps() {
		return this.getClass().getAnnotation(ProceduralAnnotation.class).steps();
	}
	public default String getBreakOut() {
		return this.getClass().getAnnotation(ProceduralAnnotation.class).breakOut();
	}
	public default StepType[] getTypes() {
		return this.getClass().getAnnotation(ProceduralAnnotation.class).types();
	}
}
