package ch.ethz.csb.youscope.shared.configuration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a conditional configuration property. Conditional properties are properties only "making sense" (=having any effect), if
 * certain conditions are fulfilled. 
 * There should exists a no-args function in the respective configuration class returning true if these conditions are fulfilled,
 * and otherwise false. This annotation should return the name of this function.
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface YSConfigConditional 
{
	/**
	 * Name of the function (no args) returning a boolean value indicating if the property has any effect.
	 * 
	 * @return Name of function.
	 */
	String value();
}
