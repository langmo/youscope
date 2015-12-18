package org.youscope.common.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Alias for a member field (i.e. a variable) or a configuration class.
 * This alias is used when automatically creating visual user interfaces for a certain configuration in an addon.
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface YSConfigAlias {
	/**
	 * Human readable name of the property.
	 * 
	 * @return human readable name.
	 */
	String value();
}
