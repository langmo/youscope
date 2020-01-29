/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.common.configuration;

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
