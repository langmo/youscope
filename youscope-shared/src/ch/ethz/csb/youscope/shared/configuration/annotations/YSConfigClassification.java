package ch.ethz.csb.youscope.shared.configuration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classification for a given configuration class. This classification is used when automatically creating a user interface for a configuration
 * to determine e.g. a folder or similar in which the given configuration should be sorted into. 
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface YSConfigClassification 
{
	/**
	 * Returns an array of strings corresponding to the classification of this configuration, e.g. to sort configurations into a folder structure.
	 * @return Classification of configuration.
	 */
	String[] value();
}
