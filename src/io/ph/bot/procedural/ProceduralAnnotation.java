package io.ph.bot.procedural;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProceduralAnnotation {
	/**
	 * Title to send in all embeds
	 * @return
	 */
	String title();
	/**
	 * Questions to ask
	 * @return
	 */
	String[] steps();
	/**
	 * Types of questions
	 * @return
	 */
	StepType[] types();
	/**
	 * String user has to input to breakout of command
	 * @return
	 */
	String breakOut();
}
