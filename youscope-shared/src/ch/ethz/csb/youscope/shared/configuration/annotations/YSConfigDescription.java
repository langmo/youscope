package ch.ethz.csb.youscope.shared.configuration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Human readable description for a member field (i.e. a variable) or a configuration class.
 * This alias is used when automatically creating visual user interfaces for a certain configuration in an addon.
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface YSConfigDescription {
	/**
	 * Human readable description of configuration or field.
	 * 
	 * @return human readable description.
	 */
	String value();
}
