package ch.ethz.csb.youscope.shared.configuration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets an icon (location) for a given configuration.
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface YSConfigIcon 
{
	/**
	 * Returns a path to an image icon representing this configuration.
	 * @return Path to an image icon representing this configuration, or null.
	 */
	String value();
}
